package org.example.service;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailAccount;
import org.example.model.EmailMessage;
import org.example.repository.EmailMessageRepository;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
@Slf4j
@RequiredArgsConstructor
public class EmailMessageService {
    private final EmailMessageRepository emailMessageRepository;

    public void sendEmail(EmailAccount emailAccount, EmailMessage emailMessage) throws MessagingException, IOException {
        Objects.requireNonNull(emailAccount, "EmailAccount cannot be null");
        Objects.requireNonNull(emailMessage, "EmailMessage cannot be null");
        if (!emailAccount.getEmailAddress().equals(emailMessage.getFrom())) {
            throw new IllegalArgumentException("Sender email address does not match the account's email address");
        }
        Session session = emailAccount.getOutgoingServerSession();
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailMessage.getFrom()));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(String.join(",", emailMessage.getTo())));
        message.setSubject(emailMessage.getSubject());

        MimeBodyPart mailBody = new MimeBodyPart();
        mailBody.setText(emailMessage.getBody());
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mailBody);

        List<File> attachments = emailMessage.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (File file : attachments) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                FileDataSource source = new FileDataSource(file);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(file.getName());
                multipart.addBodyPart(attachmentPart);
            }
        }
        message.setContent(multipart);
        Transport.send(message);
        log.info("Email sent successfully with attachments!");
    }

    public List<EmailMessage> getMessages() throws SQLException {
        return emailMessageRepository.findAll();
    }

    public List<EmailMessage> getEmailMessages(EmailAccount emailAccount) throws MessagingException, IOException, SQLException {
        Objects.requireNonNull(emailAccount, "EmailAccount cannot be null");
        Session session = emailAccount.getIncomingServerSession();
        Store store = session.getStore(emailAccount.getIncomingServer().getProtocol().name().toLowerCase());
        store.connect(emailAccount.getIncomingServer().getHost(), emailAccount.getEmailAddress(), emailAccount.getPassword());

        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        Message[] messages = folder.getMessages();

        List<EmailMessage> emailMessages = new ArrayList<>(getMessages());
        for (Message message : messages) {
            EmailMessage emailMessage = new EmailMessage();

            String[] messageIdHeader = message.getHeader("Message-ID");
            if (messageIdHeader != null && messageIdHeader.length > 0) {
                String messageId = messageIdHeader[0];
                log.info("Message-ID: " + messageId);
                emailMessage.setId(messageId);
            } else {
                log.warn("Message does not have a Message-ID.");
            }
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
            emailMessage.setSentDate(message.getSentDate());
            emailMessage.setBody(getTextFromMessage(message));
            emailMessage.setAttachments(getAttachmentsFromMessage(message));
            emailMessages.add(emailMessage);
        }
        folder.close(false);
        store.close();
        return emailMessages.stream().distinct().toList();
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


    private List<File> getAttachmentsFromMessage(Message message) throws MessagingException, IOException {
        List<File> attachments = new ArrayList<>();
        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    File file = new File(bodyPart.getFileName());
                    attachments.add(file);
                }
            }
        }
        return attachments;
    }
}