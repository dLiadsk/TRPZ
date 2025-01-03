package org.example.service;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.search.MessageIDTerm;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailMessage;
import org.example.model.common.EmailStatus;
import org.example.model.handler.EmailProtocolHandler;
import org.example.model.handler.IncomingServerHandler;
import org.example.model.handler.OutgoingServerHandler;
import org.example.repository.EmailMessageRepository;
import org.example.repository.entity.EmailMessageEntity;
import org.example.service.dto.*;
import org.example.service.mapper.EmailMessageMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailMessageService {
    private final EmailMessageRepository emailMessageRepository;
    private final EmailAttachmentService attachmentService;
    private final EmailMessageMapper emailMessageMapper;

    private final String folderControllerUrl = "http://localhost:8085/folders";
    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public EmailMessageDto getEmailMessage(EmailMessageContextDto contextDto) {
        EmailMessageEntity entity = emailMessageRepository.findById(contextDto.getId()).orElseThrow(() -> new IllegalArgumentException("Email message not found"));
        if (entity.getEmailStatus().equals(EmailStatus.UNREAD)) {
            entity.setEmailStatus(EmailStatus.READ);
            emailMessageRepository.save(entity);
        }
        return emailMessageMapper.mapToEmailMessageDto(entity);
    }

    public void sendEmail(EmailMessageSendDto emailMessageSendDto) throws MessagingException, IOException {
        EmailAccountDto emailAccountDto = emailMessageSendDto.getFrom();

        Session session = getSession(emailAccountDto, false);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailAccountDto.getEmailAddress()));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(String.join(",", emailMessageSendDto.getTo())));
        message.setSubject(emailMessageSendDto.getSubject());

        MimeBodyPart mailBody = new MimeBodyPart();
        mailBody.setText(emailMessageSendDto.getBody());
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mailBody);

        if (!emailMessageSendDto.getAttachmentPaths().isEmpty()) {
            // Завантаження файлів з attachment-service
            List<String> attachmentPaths = emailMessageSendDto.getAttachmentPaths();
            List<FileDto> attachments = attachmentService.getAttachments(attachmentPaths); // Повертаємо List<byte[]>
            for (FileDto fileDto : attachments) {
                MimeBodyPart attachmentPart = new MimeBodyPart();

                // Використовуємо ByteArrayDataSource замість File
                ByteArrayDataSource source = new ByteArrayDataSource(fileDto.getFileBytes(), fileDto.getMimeType());
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(fileDto.getName());
                multipart.addBodyPart(attachmentPart);
            }
        }
        message.setContent(multipart);
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
            throw new IllegalStateException("Email sending failed", e);
        }
        String messageId = message.getHeader("Message-ID", null);
        // Збереження у базу даних
        EmailMessageEntity entity = emailMessageMapper.mapToEntity(emailMessageSendDto);
        entity.setMessageId(messageId);
        entity.setEmailStatus(EmailStatus.SENT);
        emailMessageRepository.save(entity);
        MessagesToFolderDto messagesToTrash = new MessagesToFolderDto(List.of(entity.getMessageId()), emailAccountDto.getEmailAddress(), "Sent");
        ResponseEntity<Void> response = restTemplate.postForEntity(folderControllerUrl + "/save/messages", messagesToTrash, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Failed to save emails. Status code: " + response.getStatusCode());
        }
    }

    public void deleteEmailMessage(EmailMessageDeleteDto emailMessageDeleteDto) throws MessagingException {
        EmailMessageEntity emailMessage = emailMessageRepository.findById(emailMessageDeleteDto.getMessageId())
                .orElseThrow(() -> new IllegalArgumentException("Email message with the given ID does not exist"));

        EmailAccountDto emailAccountDto = emailMessageDeleteDto.getFrom();
        Session session = getSession(emailAccountDto, true);
        emailMessage.setEmailStatus(EmailStatus.DELETED);
        emailMessageRepository.save(emailMessage);

        MessagesToFolderDto messagesToTrash = new MessagesToFolderDto(List.of(emailMessageDeleteDto.getMessageId()), emailAccountDto.getEmailAddress(), "Trash");
        ResponseEntity<Void> response = restTemplate.postForEntity(folderControllerUrl + "/save/messages", messagesToTrash, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Failed to save emails. Status code: " + response.getStatusCode());
        }
        MessagesToFolderDto messagesDeleteFromInbox = new MessagesToFolderDto(List.of(emailMessageDeleteDto.getMessageId()), emailAccountDto.getEmailAddress(), "Inbox");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MessagesToFolderDto> requestEntity = new HttpEntity<>(messagesDeleteFromInbox, headers);
        restTemplate.exchange(
                folderControllerUrl + "/delete/from/folder",
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );
        MessagesToFolderDto messagesDeleteFromAll = new MessagesToFolderDto(List.of(emailMessageDeleteDto.getMessageId()), emailAccountDto.getEmailAddress(), "All");
        HttpEntity<MessagesToFolderDto> request = new HttpEntity<>(messagesDeleteFromAll, headers);
        restTemplate.exchange(
                folderControllerUrl + "/delete/from/folder",
                HttpMethod.DELETE,
                request,
                Void.class
        );
        try (Store store = session.getStore("imap")) {
            store.connect(emailAccountDto.getIncomingServer().getHost(), emailAccountDto.getEmailAddress(), emailAccountDto.getPassword());

            Folder sourceFolder = store.getFolder("INBOX");
            sourceFolder.open(Folder.READ_WRITE);

            // Знаходимо повідомлення за унікальним ID
            Message[] messages = sourceFolder.search(new MessageIDTerm(emailMessage.getMessageId()));
            if (messages.length == 0) {
                throw new IllegalArgumentException("Email message not found in the INBOX.");
            }

            Message messageToDelete = messages[0];


            // Перевірка, чи повідомлення не видалене
            if (messageToDelete.isExpunged()) {
                throw new IllegalArgumentException("Email message is already removed.");
            }

            // Переміщення до папки Trash
            Folder trashFolder = store.getFolder("Trash");
            if (!trashFolder.exists()) {
                trashFolder.create(Folder.HOLDS_MESSAGES);
            }
            trashFolder.open(Folder.READ_WRITE);
            sourceFolder.copyMessages(new Message[]{messageToDelete}, trashFolder);
            messageToDelete.setFlag(Flags.Flag.DELETED, true); // Позначаємо для видалення

            sourceFolder.close(false);
            trashFolder.close(false);
        } catch (MessageRemovedException e) {
            log.warn("Email message already removed: {}", e.getMessage());
        } catch (MessagingException e) {
            log.error("Failed to delete email message: {}", e.getMessage());
            throw e;
        }

        log.info("Email message with ID {} has been deleted.", emailMessage.getMessageId());

    }

    public void saveDraft(EmailMessageSendDto emailMessageSendDto) throws MessagingException {
        EmailAccountDto emailAccountDto = emailMessageSendDto.getFrom();

        Session session = getSession(emailAccountDto, true);
        String messageId;
        if (session == null) {
            messageId = UUID.randomUUID().toString();
        } else {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailAccountDto.getEmailAddress()));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(String.join(",", emailMessageSendDto.getTo())));
            message.setSubject(emailMessageSendDto.getSubject());
            MimeBodyPart mailBody = new MimeBodyPart();
            mailBody.setText(emailMessageSendDto.getBody());
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mailBody);
            if (!emailMessageSendDto.getAttachmentPaths().isEmpty()) {
                // Завантаження файлів з attachment-service
                List<String> attachmentPaths = emailMessageSendDto.getAttachmentPaths();
                List<FileDto> attachments = attachmentService.getAttachments(attachmentPaths); // Повертаємо List<byte[]>
                for (FileDto fileDto : attachments) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();

                    // Використовуємо ByteArrayDataSource замість File
                    ByteArrayDataSource source = new ByteArrayDataSource(fileDto.getFileBytes(), fileDto.getMimeType());
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(fileDto.getName()); // Унікальна назва для кожного вкладення

                    multipart.addBodyPart(attachmentPart);
                }
            }
            message.setContent(multipart);

            Store store = session.getStore("imap");
            store.connect(emailAccountDto.getIncomingServer().getHost(), emailAccountDto.getEmailAddress(), emailAccountDto.getPassword());
            Folder draftsFolder = store.getFolder("Drafts");
            if (!draftsFolder.exists()) {
                draftsFolder.create(Folder.HOLDS_MESSAGES);
            }
            draftsFolder.open(Folder.READ_WRITE);
            message.saveChanges();
            draftsFolder.appendMessages(new Message[]{message});
            draftsFolder.close(false);
            store.close();
            messageId = message.getHeader("Message-ID", null);
        }
        // Збереження у базу даних
        EmailMessageEntity entity = emailMessageMapper.mapToEntity(emailMessageSendDto);
        entity.setMessageId(messageId);
        entity.setEmailStatus(EmailStatus.DRAFT);

        emailMessageRepository.save(entity);

        MessagesToFolderDto messages = new MessagesToFolderDto(List.of(messageId), emailAccountDto.getEmailAddress(), "Drafts");
        ResponseEntity<Void> response = restTemplate.postForEntity(folderControllerUrl + "/save/messages", messages, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Failed to save emails. Status code: " + response.getStatusCode());
        }
    }

    public List<EmailMessageContextDto> getEmailMessages(List<EmailAccountDto> emailAccounts) throws MessagingException, IOException {
        Objects.requireNonNull(emailAccounts, "EmailAccounts cannot be null");

        // Завантаження існуючих повідомлень з бази
        Set<String> existingMessageIds = new HashSet<>(emailMessageRepository.findAllMessageIds());
        List<EmailMessageContextDto> messages = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (EmailAccountDto emailAccount : emailAccounts) {
            if (emailAccount == null) {
                log.warn("Null EmailAccountDto encountered, skipping.");
                continue;
            }
            // Підключення до поштового серверу
            Session session = getSession(emailAccount, true);
            if (session != null) {
                Store store = session.getStore(emailAccount.getIncomingServer().getProtocol().toLowerCase());
                store.connect(emailAccount.getIncomingServer().getHost(), emailAccount.getEmailAddress(), emailAccount.getPassword());
                Folder rootFolder = store.getDefaultFolder();
                Folder[] folders = rootFolder.list();
                for (Folder folder : folders) {
                    saveMessagesFromFolder(store, existingMessageIds, emailAccount, folder.getName());
                }
                store.close();
            }

            HttpEntity<EmailAddressForFolderDto> requestEntity = new HttpEntity<>(new EmailAddressForFolderDto("All", emailAccount.getEmailAddress()), headers);
            ResponseEntity<List<EmailMessageContextDto>> response = restTemplate.exchange(
                    folderControllerUrl + "/get/emails/from/folder",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                messages.addAll(Objects.requireNonNull(response.getBody()));
            }
        }
        // Повертаємо всі повідомлення (з бази + нові)
        return messages.stream()
                .sorted(Comparator.comparing(EmailMessageContextDto::getSentDate))
                .collect(Collectors.toList());
    }

    private void saveMessagesFromFolder(Store store, Set<String> existingMessageIds, EmailAccountDto emailAccount, String folderName) throws MessagingException, IOException {
        Folder folder = store.getFolder(folderName);
        List<String> messagesToSaveInFolder = new ArrayList<>();

        try (folder) {
            folder.open(Folder.READ_ONLY);
            Message[] messages = folder.getMessages();
            List<EmailMessageEntity> newEntities = new ArrayList<>();
            for (Message message : messages) {
                String[] messageIdHeader = message.getHeader("Message-ID");
                String messageId = (messageIdHeader != null && messageIdHeader.length > 0) ? messageIdHeader[0] : null;
                messagesToSaveInFolder.add(messageId);
                // Пропустити, якщо Message-ID вже існує
                if (messageId != null && existingMessageIds.contains(messageId)) {
                    continue;
                }
                if (messageId == null) {
                    log.warn("Message without Message-ID encountered, skipping.");
                    continue;
                }
                EmailMessage emailMessage = new EmailMessage();
                emailMessage.setMessageId(messageId);
                emailMessage.setFrom(((InternetAddress) message.getFrom()[0]).getAddress());

                Address[] recipients = message.getRecipients(Message.RecipientType.TO);
                if (recipients != null) {
                    List<String> recipientList = new ArrayList<>();
                    for (Address recipient : recipients) {
                        recipientList.add(((InternetAddress) recipient).getAddress());
                    }
                    emailMessage.setTo(recipientList);
                }

                emailMessage.setSubject(message.getSubject());
                emailMessage.setSentDate(message.getSentDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime());

                if (message.isMimeType("multipart/*")) {
                    List<String> attachmentPaths = new ArrayList<>();
                    StringBuilder bodyContent = new StringBuilder();
                    extractAttachmentsAndBody((Multipart) message.getContent(), attachmentPaths, bodyContent);
                    emailMessage.setBody(bodyContent.toString());
                    emailMessage.setAttachmentNames(attachmentPaths);
                } else {
                    emailMessage.setBody(getTextFromMessage(message));
                }

                Flags flags = message.getFlags();
                if (flags.contains(Flags.Flag.SEEN)) {
                    emailMessage.setEmailStatus(EmailStatus.READ);
                } else if (flags.contains(Flags.Flag.DELETED)) {
                    emailMessage.setEmailStatus(EmailStatus.DELETED);
                } else if (flags.contains(Flags.Flag.DRAFT)) {
                    emailMessage.setEmailStatus(EmailStatus.DRAFT);
                } else {
                    emailMessage.setEmailStatus(EmailStatus.UNREAD);
                }

                EmailMessageEntity entity = emailMessageMapper.mapToEntity(emailMessage);
                newEntities.add(entity);
                emailMessageRepository.save(entity);
            }
            emailMessageRepository.saveAll(newEntities);
        }
        MessagesToFolderDto messages = new MessagesToFolderDto(messagesToSaveInFolder, emailAccount.getEmailAddress(), folderName);
        ResponseEntity<Void> response = restTemplate.postForEntity(folderControllerUrl + "/save/messages", messages, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException("Failed to save emails. Status code: " + response.getStatusCode());
        }

    }

    private Session getSession(EmailAccountDto emailAccount, boolean incoming) {
        if (incoming) {
            EmailProtocolHandler incomingHandler = new IncomingServerHandler();
            Session incomingSession = incomingHandler.authorize(emailAccount, emailAccount.getIncomingServer());
            if (incomingSession == null) {
                log.warn("Authorization failed on incoming server for: " + emailAccount.getEmailAddress());
                return null;
            }
            return incomingSession;
        } else {
            EmailProtocolHandler outgoingHandler = new OutgoingServerHandler();
            Session outgoingSession = outgoingHandler.authorize(emailAccount, emailAccount.getOutgoingServer());
            if (outgoingSession == null) {
                log.warn("Authorization failed on outgoing server for: " + emailAccount.getEmailAddress());
                return null;
            }
            return outgoingSession;
        }
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return (String) message.getContent();
        } else if (message.isMimeType("text/html")) {
            return (String) message.getContent();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    return (String) bodyPart.getContent();
                } else if (bodyPart.isMimeType("text/html")) {
                    return (String) bodyPart.getContent();
                }
            }
        }
        return null;
    }

    private void extractAttachmentsAndBody(Multipart multipart, List<String> attachmentPaths, StringBuilder bodyContent) throws MessagingException, IOException {
        List<FileDto> attachmentFiles = new ArrayList<>();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            String disposition = bodyPart.getDisposition();

            // Обробка вкладень
            if (Part.ATTACHMENT.equalsIgnoreCase(disposition) ||
                    (disposition == null && bodyPart.getFileName() != null)) {
                String fileName = MimeUtility.decodeText(bodyPart.getFileName());
                String mimeType = bodyPart.getContentType().split(";")[0].trim();
                try (InputStream inputStream = bodyPart.getInputStream()) {
                    byte[] bytes = inputStream.readAllBytes();
                    attachmentFiles.add(new FileDto(bytes, fileName, mimeType));
                }
            }
            // Обробка тексту (text/plain або text/html)
            if (bodyPart.isMimeType("text/plain")) {
                bodyContent.append(bodyPart.getContent().toString());
            }
            // Рекурсивна обробка вкладених MIME-частин
            if (bodyPart.isMimeType("multipart/*")) {
                extractAttachmentsAndBody((Multipart) bodyPart.getContent(), attachmentPaths, bodyContent);
            }
        }
        attachmentPaths.addAll(attachmentService.downloadAttachments(attachmentFiles));
    }


}