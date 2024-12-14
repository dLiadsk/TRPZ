package org.example.model.decorator;

import org.example.model.EmailMessage;

import java.util.List;

public class BaseFilter implements EmailFilter{
    @Override
    public List<EmailMessage> filter(List<EmailMessage> emails) {
        return emails;
    }
}
