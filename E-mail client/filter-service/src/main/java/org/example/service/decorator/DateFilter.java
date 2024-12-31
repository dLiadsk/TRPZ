package org.example.service.decorator;

import org.example.dto.EmailMessageContextDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DateFilter extends FilterDecorator {
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public DateFilter(EmailFilter filter, LocalDateTime startDate, LocalDateTime endDate) {
        super(filter);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public List<EmailMessageContextDto> filter(List<EmailMessageContextDto> emails) {
        return super.filter(emails).stream()
                .filter(email -> email.getSentDate().isAfter(startDate) && email.getSentDate().isBefore(endDate))
                .collect(Collectors.toList());
    }
}