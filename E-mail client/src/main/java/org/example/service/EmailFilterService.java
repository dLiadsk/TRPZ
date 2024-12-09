package org.example.service;

import org.example.model.EmailMessage;
import org.example.model.decorator.BaseFilter;
import org.example.model.decorator.DateFilter;
import org.example.model.decorator.EmailFilter;
import org.example.model.decorator.UnreadFilter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class EmailFilterService {

    public List<EmailMessage> emailUnreadMessageDateFilter(List<EmailMessage> emailMessages, LocalDate startDate, LocalDate endDate) throws SQLException {
        EmailFilter filter = new UnreadFilter(new DateFilter(new BaseFilter(), startDate, endDate));
        return filter.filter(emailMessages).stream().sorted(Comparator.comparing(EmailMessage::getSentDate)).toList();
    }
}
