package org.example.service.interpreter;

import org.example.model.EmailMessage;
import org.example.model.common.EmailStatus;

public class StatusFilter implements Expression {
    private EmailStatus status;

    public StatusFilter(EmailStatus status) {
        this.status = status;
    }

    @Override
    public boolean interpret(EmailMessage emailMessage) {
        return emailMessage.getEmailStatus() == status;
    }
}
