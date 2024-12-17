package org.example.service.interpreter;

import org.example.model.EmailMessage;

public class SenderFilter implements Expression {
    private String sender;

    public SenderFilter(String sender) {
        this.sender = sender;
    }

    @Override
    public boolean interpret(EmailMessage emailMessage) {
        return emailMessage.getFrom() != null && emailMessage.getFrom().equalsIgnoreCase(sender);
    }
}