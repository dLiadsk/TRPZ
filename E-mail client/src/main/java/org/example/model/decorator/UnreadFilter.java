package org.example.model.decorator;

import org.example.model.EmailMessage;
import org.example.model.common.EmailStatus;

import java.util.List;
import java.util.stream.Collectors;

public class UnreadFilter extends FilterDecorator {
    public UnreadFilter(EmailFilter filter) {
        super(filter);
    }

    @Override
    public List<EmailMessage> filter(List<EmailMessage> emails) {
        return emails.stream()
                .filter(email -> email.getEmailStatus() != null && email.getEmailStatus().equals(EmailStatus.UNREAD))
                .collect(Collectors.toList());
    }
}