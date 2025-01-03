package org.example.service.interpreter;

import org.example.dto.EmailMessageContextDto;

public class SenderFilter implements Expression {
    private String sender;

    public SenderFilter(String sender) {
        this.sender = sender;
    }

    @Override
    public boolean interpret(EmailMessageContextDto emailMessage) {
        return emailMessage.getFrom() != null && emailMessage.getFrom().equalsIgnoreCase(sender);
    }
}