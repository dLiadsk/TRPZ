package org.example.model.decorator;

import org.example.model.EmailMessage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DateFilter extends FilterDecorator {
    private LocalDate startDate;
    private LocalDate endDate;

    public DateFilter(EmailFilter filter, LocalDate startDate, LocalDate endDate) {
        super(filter);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public List<EmailMessage> filter(List<EmailMessage> emails) {
        return super.filter(emails).stream()
                .filter(email -> email.getSentDate().isAfter(startDate) && email.getSentDate().isBefore(endDate))
                .collect(Collectors.toList());
    }
}