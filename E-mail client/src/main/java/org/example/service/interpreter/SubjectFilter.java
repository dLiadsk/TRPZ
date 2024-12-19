package org.example.service.interpreter;

import org.example.model.EmailMessage;

public class SubjectFilter implements Expression {
    private String keyword;

    public SubjectFilter(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public boolean interpret(EmailMessage emailMessage) {
        return emailMessage.getSubject() != null && emailMessage.getSubject().contains(keyword);
    }
}
