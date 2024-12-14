package org.example.model.decorator;

import org.example.model.EmailMessage;

import java.util.List;

abstract class FilterDecorator implements EmailFilter {
    protected EmailFilter filter;

    public FilterDecorator(EmailFilter filter) {
        this.filter = filter;
    }

    @Override
    public List<EmailMessage> filter(List<EmailMessage> emails) {
        return filter.filter(emails);
    }
}