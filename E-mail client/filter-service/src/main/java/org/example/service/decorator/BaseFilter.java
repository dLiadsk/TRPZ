package org.example.service.decorator;

import org.example.dto.EmailMessageContextDto;

import java.util.List;

public class BaseFilter implements EmailFilter{
    @Override
    public List<EmailMessageContextDto> filter(List<EmailMessageContextDto> emails) {
        return emails;
    }
}
