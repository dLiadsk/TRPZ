package org.example.service.interpreter;

import org.example.dto.EmailMessageContextDto;

public class SubjectFilter implements Expression {
    private String keyword;

    public SubjectFilter(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public boolean interpret(EmailMessageContextDto emailMessage) {
        return emailMessage.getSubject() != null && emailMessage.getSubject().toLowerCase().contains(keyword);
    }
}
