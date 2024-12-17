package org.example.service;

import org.example.model.EmailMessage;
import org.example.model.common.EmailStatus;
import org.example.service.decorator.BaseFilter;
import org.example.service.decorator.DateFilter;
import org.example.service.decorator.EmailFilter;
import org.example.service.decorator.UnreadFilter;
import org.example.service.interpreter.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EmailFilterService {

    public List<EmailMessage> emailUnreadMessageDateFilter(List<EmailMessage> emailMessages, LocalDate startDate, LocalDate endDate) throws SQLException {
        EmailFilter filter = new UnreadFilter(new DateFilter(new BaseFilter(), startDate, endDate));
        return filter.filter(emailMessages).stream().sorted(Comparator.comparing(EmailMessage::getSentDate)).toList();
    }

    public List<EmailMessage> getMessageBySender(List<EmailMessage> messages, String email){
        if (email == null || email.isEmpty()) return Collections.emptyList();
        return filterMessages(messages, new SenderFilter(email));
    }

    public List<EmailMessage> getMessageBySubject(List<EmailMessage> messages, String keyWord){
        if (keyWord == null) return Collections.emptyList();
        return filterMessages(messages, new SubjectFilter(keyWord));
    }

    public List<EmailMessage> getMessageByStatus(List<EmailMessage> messages, EmailStatus status){
        if (status == null) return Collections.emptyList();
        return filterMessages(messages, new StatusFilter(status));
    }
    public List<EmailMessage> getMessageByStatusAndSender(List<EmailMessage> messages, EmailStatus status, String email){
        if (status == null || email == null || email.isEmpty()) return Collections.emptyList();
        return filterMessages(messages, new AndFilter(new StatusFilter(status), new SenderFilter(email)));
    }
    public List<EmailMessage> getMessageBySubjectOrSender(List<EmailMessage> messages, String keyWord, String email){
        if (keyWord == null || email == null || email.isEmpty()) return Collections.emptyList();
        return filterMessages(messages, new OrFilter(new SubjectFilter(keyWord), new SenderFilter(email)));
    }
    private List<EmailMessage> filterMessages(List<EmailMessage> messages, Expression expression) {
        if (messages == null || messages.isEmpty()) return Collections.emptyList();
        return messages.stream()
                .filter(expression::interpret)
                .collect(Collectors.toList());
    }
}
