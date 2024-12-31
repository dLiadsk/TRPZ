package org.example.service.mapper;

import org.example.model.EmailMessage;
import org.example.model.common.EmailStatus;
import org.example.repository.entity.EmailMessageEntity;
import org.example.service.dto.EmailMessageContextDto;
import org.example.service.dto.EmailMessageDto;
import org.example.service.dto.EmailMessageSendDto;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public class EmailMessageMapper {

    public EmailMessageEntity mapToEntity(EmailMessage emailMessage) {
        EmailMessageEntity entity = new EmailMessageEntity();
        entity.setMessageId(emailMessage.getMessageId());
        entity.setSubject(emailMessage.getSubject());
        entity.setFrom(emailMessage.getFrom());
        entity.setTo(emailMessage.getTo());
        entity.setSentDate(emailMessage.getSentDate());
        entity.setBody(emailMessage.getBody());
        entity.setAttachmentPaths(emailMessage.getAttachmentNames());
        entity.setEmailStatus(emailMessage.getEmailStatus());
        return entity;
    }
    public EmailMessageEntity mapToEntity(EmailMessageSendDto emailMessage, String id) {
        EmailMessageEntity entity = new EmailMessageEntity();
        entity.setMessageId(id);
        entity.setSubject(emailMessage.getSubject());
        entity.setFrom(emailMessage.getFrom().getEmailAddress());
        entity.setTo(emailMessage.getTo());
        entity.setSentDate(LocalDateTime.now());
        entity.setBody(emailMessage.getBody());
        entity.setAttachmentPaths(emailMessage.getAttachmentPaths());
        return entity;
    }

    public EmailMessageContextDto mapToEmailMessageContextDto(EmailMessageEntity entity) {
        return new EmailMessageContextDto(entity.getMessageId(), entity.getSubject(), entity.getFrom(), entity.getSentDate(), entity.getEmailStatus().name());
    }

    public EmailMessage mapToModel(EmailMessageEntity entity) {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setMessageId(entity.getMessageId());
        emailMessage.setSubject(entity.getSubject());
        emailMessage.setFrom(entity.getFrom());
        emailMessage.setTo(entity.getTo());
        emailMessage.setSentDate(entity.getSentDate());
        emailMessage.setBody(entity.getBody());
        emailMessage.setAttachmentNames(entity.getAttachmentPaths());
        emailMessage.setEmailStatus(entity.getEmailStatus());
        return emailMessage;
    }

    public EmailMessageDto mapToEmailMessageDto(EmailMessageEntity entity) {
        EmailMessageDto messageDto = new EmailMessageDto(entity.getMessageId(), entity.getSubject(), entity.getFrom(), entity.getTo(), entity.getSentDate(), entity.getBody(), entity.getAttachmentPaths(), entity.getEmailStatus().name());
        return messageDto;
    }
}
