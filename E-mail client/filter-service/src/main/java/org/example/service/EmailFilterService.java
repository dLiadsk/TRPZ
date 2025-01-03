package org.example.service;

import org.example.common.EmailStatus;
import org.example.dto.EmailMessageContextDto;
import org.example.dto.FilterDto;
import org.example.service.decorator.BaseFilter;
import org.example.service.decorator.DateFilter;
import org.example.service.decorator.EmailFilter;
import org.example.service.decorator.StatusFilter;
import org.example.service.interpreter.*;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailFilterService {
    public List<EmailMessageContextDto> filter(FilterDto filterDto) {
        boolean statusIsEmpty = filterDto.getStatus() == null || filterDto.getStatus().isEmpty();
        boolean senderIsEmpty = filterDto.getSender() == null || filterDto.getSender().isEmpty();
        boolean subjectIsEmpty = filterDto.getSubject() == null || filterDto.getSubject().isEmpty();
        boolean fromDateIsEmpty = filterDto.getFrom() == null;
        boolean toDateIsEmpty = filterDto.getTo() == null;

        if (!fromDateIsEmpty && toDateIsEmpty) {
            filterDto.setTo(LocalDateTime.now());
            toDateIsEmpty = false;
        }
        if (senderIsEmpty && statusIsEmpty && subjectIsEmpty && fromDateIsEmpty) {
            return filterDto.getEmails();
        }
        if (senderIsEmpty && statusIsEmpty && subjectIsEmpty) {
            return emailDateFilter(filterDto.getEmails(), filterDto.getFrom(), filterDto.getTo());
        }
        if (senderIsEmpty && statusIsEmpty && fromDateIsEmpty) {
            return getMessageBySubject(filterDto.getEmails(), filterDto.getSubject());
        }
        if (senderIsEmpty && fromDateIsEmpty && subjectIsEmpty) {
            return emailStatusFilter(filterDto.getEmails(), filterDto.getStatus());
        }
        if (fromDateIsEmpty && statusIsEmpty && subjectIsEmpty) {
            return getMessageBySender(filterDto.getEmails(), filterDto.getSender());
        }
        if (senderIsEmpty && statusIsEmpty) {
            return getMessageBySubject(emailDateFilter(filterDto.getEmails(), filterDto.getFrom(), filterDto.getTo()), filterDto.getSubject());
        }

        if (senderIsEmpty && subjectIsEmpty) {
            return emailStatusMessageDateFilter(filterDto.getEmails(), filterDto.getFrom(), filterDto.getTo(), filterDto.getStatus());
        }
        if (statusIsEmpty && subjectIsEmpty) {
            return getMessageBySender(emailDateFilter(filterDto.getEmails(), filterDto.getFrom(), filterDto.getTo()), filterDto.getSender());
        }
        if (senderIsEmpty && fromDateIsEmpty) {
            return getMessageByStatusAndSubject(filterDto.getEmails(), filterDto.getStatus(), filterDto.getSubject());
        }
        if (subjectIsEmpty && fromDateIsEmpty) {
            return getMessageByStatusAndSender(filterDto.getEmails(), filterDto.getStatus(), filterDto.getSender());
        }
        if (statusIsEmpty && fromDateIsEmpty) {
            return getMessageBySubjectAndSender(filterDto.getEmails(), filterDto.getStatus(), filterDto.getSubject());
        }
        if (senderIsEmpty) {
            return getMessageBySubject(emailStatusMessageDateFilter(filterDto.getEmails(), filterDto.getFrom(), filterDto.getTo(), filterDto.getStatus()), filterDto.getSubject());
        }
        if (fromDateIsEmpty) {
            return filterMessages(filterDto.getEmails(), new AndFilter(new AndFilter(new SenderFilter(filterDto.getSender()), new org.example.service.interpreter.StatusFilter(filterDto.getStatus())), new SubjectFilter(filterDto.getSubject())));
        }
        if (subjectIsEmpty) {
            return getMessageBySender(emailStatusMessageDateFilter(filterDto.getEmails(), filterDto.getFrom(), filterDto.getTo(), filterDto.getStatus()), filterDto.getSender());
        }
        if (statusIsEmpty) {
            return filterMessages(emailDateFilter(filterDto.getEmails(), filterDto.getFrom(), filterDto.getTo()), new AndFilter(new SenderFilter(filterDto.getSender()), new SubjectFilter(filterDto.getSubject())));
        } else {
            return filterMessages(emailDateFilter(filterDto.getEmails(), filterDto.getFrom(), filterDto.getTo()), new AndFilter(new AndFilter(new SenderFilter(filterDto.getSender()), new org.example.service.interpreter.StatusFilter(filterDto.getStatus())), new SubjectFilter(filterDto.getSubject())));
        }

    }

    private List<EmailMessageContextDto> emailDateFilter(List<EmailMessageContextDto> emailMessages, LocalDateTime startDate, LocalDateTime endDate) {
        EmailFilter filter = new DateFilter(new BaseFilter(), startDate, endDate);
        return filter.filter(emailMessages).stream().sorted(Comparator.comparing(EmailMessageContextDto::getSentDate)).toList();
    }

    private List<EmailMessageContextDto> emailStatusFilter(List<EmailMessageContextDto> emailMessages, String status) {
        EmailFilter filter = new StatusFilter(new BaseFilter(), status);
        return filter.filter(emailMessages).stream().sorted(Comparator.comparing(EmailMessageContextDto::getSentDate)).toList();
    }

    private List<EmailMessageContextDto> emailStatusMessageDateFilter(List<EmailMessageContextDto> emailMessages, LocalDateTime startDate, LocalDateTime endDate, String status) {
        EmailFilter filter = new StatusFilter(new DateFilter(new BaseFilter(), startDate, endDate), status);
        return filter.filter(emailMessages).stream().sorted(Comparator.comparing(EmailMessageContextDto::getSentDate)).toList();
    }

    private List<EmailMessageContextDto> getMessageBySender(List<EmailMessageContextDto> messages, String email) {
        if (email == null || email.isEmpty()) return Collections.emptyList();
        return filterMessages(messages, new SenderFilter(email));
    }

    private List<EmailMessageContextDto> getMessageBySubject(List<EmailMessageContextDto> messages, String keyWord) {
        if (keyWord == null) return Collections.emptyList();
        return filterMessages(messages, new SubjectFilter(keyWord));
    }

    private List<EmailMessageContextDto> getMessageByStatus(List<EmailMessageContextDto> messages, String status) {
        if (status == null) return Collections.emptyList();
        return filterMessages(messages, new org.example.service.interpreter.StatusFilter(status));
    }

    private List<EmailMessageContextDto> getMessageByStatusAndSender(List<EmailMessageContextDto> messages, String status, String email) {
        if (status == null || email == null || email.isEmpty()) return Collections.emptyList();
        return filterMessages(messages, new AndFilter(new org.example.service.interpreter.StatusFilter(status), new SenderFilter(email)));
    }

    private List<EmailMessageContextDto> getMessageByStatusAndSubject(List<EmailMessageContextDto> messages, String status, String subject) {
        if (status == null || subject == null || subject.isEmpty()) return Collections.emptyList();
        return filterMessages(messages, new AndFilter(new org.example.service.interpreter.StatusFilter(status), new SubjectFilter(subject)));
    }

    private List<EmailMessageContextDto> getMessageBySubjectAndSender(List<EmailMessageContextDto> messages, String keyWord, String email) {
        if (keyWord == null || email == null || email.isEmpty()) return Collections.emptyList();
        return filterMessages(messages, new AndFilter(new SubjectFilter(keyWord), new SenderFilter(email)));
    }

    private List<EmailMessageContextDto> filterMessages(List<EmailMessageContextDto> messages, Expression expression) {
        if (messages == null || messages.isEmpty()) return Collections.emptyList();
        return messages.stream()
                .filter(expression::interpret)
                .collect(Collectors.toList());
    }
}
