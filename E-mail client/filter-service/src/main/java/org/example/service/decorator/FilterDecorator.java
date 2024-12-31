package org.example.service.decorator;

import org.example.dto.EmailMessageContextDto;

import java.util.List;

abstract class FilterDecorator implements EmailFilter {
    protected EmailFilter filter;

    public FilterDecorator(EmailFilter filter) {
        this.filter = filter;
    }

    @Override
    public List<EmailMessageContextDto> filter(List<EmailMessageContextDto> emails) {
        return filter.filter(emails);
    }
}