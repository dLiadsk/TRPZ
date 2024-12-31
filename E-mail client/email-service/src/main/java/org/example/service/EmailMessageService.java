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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.*;

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
        if (entity.getEmailStatus().equals(EmailStatus.UNREAD)){
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
            List<FileDto> attachments = attachmentService.downloadAttachments(attachmentPaths); // Повертаємо List<byte[]>
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
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
            throw new IllegalStateException("Email sending failed", e);
        }
        String messageId = message.getHeader("Message-ID", null);
        // Збереження у базу даних
        EmailMessageEntity entity = emailMessageMapper.mapToEntity(emailMessageSendDto, messageId);
        entity.setEmailStatus(EmailStatus.SENT);
        emailMessageRepository.save(entity);
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
        if (!response.getStatusCode().is2xxSuccessful()){
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
        }catch (MessageRemovedException e) {
            log.warn("Email message already removed: {}", e.getMessage());
        } catch (MessagingException e) {
            log.error("Failed to delete email message: {}", e.getMessage());
            throw e;
        }

        log.info("Email message with ID {} has been deleted.", emailMessage.getMessageId());

    }

    @Transactional
    public void saveDraft(EmailMessageSendDto emailMessageSendDto) throws MessagingException {
        EmailAccountDto emailAccountDto = emailMessageSendDto.getFrom();

        Session session = getSession(emailAccountDto, true);
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
            List<FileDto> attachments = attachmentService.downloadAttachments(attachmentPaths); // Повертаємо List<byte[]>
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
        String messageId = message.getHeader("Message-ID", null);
        // Збереження у базу даних
        EmailMessageEntity entity = emailMessageMapper.mapToEntity(emailMessageSendDto, messageId);
        emailMessageRepository.save(entity);

        MessagesToFolderDto messages = new MessagesToFolderDto(List.of(messageId), emailAccountDto.getEmailAddress(), "Draft");
        ResponseEntity<Void> response = restTemplate.postForEntity(folderControllerUrl + "/save/messages", messages, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()){
            throw new IllegalArgumentException("Failed to save emails. Status code: " + response.getStatusCode());
        }
    }

    public List<EmailMessageContextDto> getEmailMessages(List<EmailAccountDto> emailAccounts) throws MessagingException, IOException, SQLException {
        Objects.requireNonNull(emailAccounts, "EmailAccounts cannot be null");

        // Завантаження існуючих повідомлень з бази
        Set<String> existingMessageIds = new HashSet<>(emailMessageRepository.findAllMessageIds());

        for (EmailAccountDto emailAccount : emailAccounts) {
            if (emailAccount == null) {
                log.warn("Null EmailAccountDto encountered, skipping.");
                continue;
            }
            // Підключення до поштового серверу
            Session session = getSession(emailAccount, true);
            Store store = session.getStore(emailAccount.getIncomingServer().getProtocol().toLowerCase());
            store.connect(emailAccount.getIncomingServer().getHost(), emailAccount.getEmailAddress(), emailAccount.getPassword());
            Folder rootFolder = store.getDefaultFolder();

            Folder[] folders = rootFolder.list();

            for (Folder folder : folders) {
                saveMessagesFromFolder(store, existingMessageIds, emailAccount, folder.getName());
            }
            store.close();
        }
        // Повертаємо всі повідомлення (з бази + нові)
        return emailMessageRepository.findAll().stream()
                .map(emailMessageMapper::mapToEmailMessageContextDto)
                .toList();
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
                        .toLocalDateTime()); // Використовуємо LocalDateTime
                emailMessage.setBody(getTextFromMessage(message));
                emailMessage.setAttachmentNames(getAttachmentNamesFromMessage(message));

                Flags flags = message.getFlags();
                if (flags.contains(Flags.Flag.SEEN)) {
                    emailMessage.setEmailStatus(EmailStatus.READ); // Переглянуте
                } else if (flags.contains(Flags.Flag.DELETED)) {
                    emailMessage.setEmailStatus(EmailStatus.DELETED); // Непрочитане
                } else if (flags.contains(Flags.Flag.DRAFT)) {
                    emailMessage.setEmailStatus(EmailStatus.DRAFT); // Непрочитане
                } else {
                    emailMessage.setEmailStatus(EmailStatus.UNREAD); // Непрочитане
                }

                EmailMessageEntity entity = emailMessageMapper.mapToEntity(emailMessage);
                newEntities.add(entity);
                emailMessageRepository.save(entity);
            }
            emailMessageRepository.saveAll(newEntities);
        }
        MessagesToFolderDto messages = new MessagesToFolderDto(messagesToSaveInFolder, emailAccount.getEmailAddress(), folderName);
        ResponseEntity<Void> response = restTemplate.postForEntity(folderControllerUrl + "/save/messages", messages, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()){
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

    private List<String> getAttachmentNamesFromMessage(Message message) throws MessagingException, IOException {
        List<String> attachmentNames = new ArrayList<>();
        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disposition = bodyPart.getDisposition();

                // Перевіряємо чи це вкладення
                if (Part.ATTACHMENT.equalsIgnoreCase(disposition) ||
                        (disposition == null && bodyPart.getFileName() != null)) {
                    attachmentNames.add(bodyPart.getFileName());
                }

                // Рекурсивна перевірка для вкладених MIME
                if (bodyPart.isMimeType("multipart/*")) {
                    Multipart nestedMultipart = (Multipart) bodyPart.getContent();
                    for (int j = 0; j < nestedMultipart.getCount(); j++) {
                        BodyPart nestedBodyPart = nestedMultipart.getBodyPart(j);
                        String nestedDisposition = nestedBodyPart.getDisposition();
                        if (Part.ATTACHMENT.equalsIgnoreCase(nestedDisposition) ||
                                (nestedDisposition == null && nestedBodyPart.getFileName() != null)) {
                            attachmentNames.add(nestedBodyPart.getFileName());
                        }
                    }
                }
            }
        }
        return attachmentNames;
    }



}