package org.example.service.decorator;

import org.example.dto.EmailMessageContextDto;


import java.util.List;
import java.util.stream.Collectors;

public class StatusFilter extends FilterDecorator {
    private final String status;
    public StatusFilter(EmailFilter filter, String status) {
        super(filter);
        this.status = status;
    }

    @Override
    public List<EmailMessageContextDto> filter(List<EmailMessageContextDto> emails) {
        return emails.stream()
                .filter(email -> email.getEmailStatus() != null && email.getEmailStatus().equals(status))
                .collect(Collectors.toList());
    }
}