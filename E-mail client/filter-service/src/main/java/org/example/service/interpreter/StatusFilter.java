package org.example.service.interpreter;

import org.example.common.EmailStatus;
import org.example.dto.EmailMessageContextDto;


public class StatusFilter implements Expression {
    private String status;

    public StatusFilter(String status) {
        this.status = status;
    }

    @Override
    public boolean interpret(EmailMessageContextDto emailMessage) {
        return emailMessage.getEmailStatus().equals(status);
    }
}
