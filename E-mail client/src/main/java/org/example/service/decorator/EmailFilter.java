package org.example.service.decorator;

import org.example.model.EmailMessage;

import java.util.List;

public interface EmailFilter {
    List<EmailMessage> filter(List<EmailMessage> emails);
}
