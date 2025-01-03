package org.example.service.decorator;

import org.example.dto.EmailMessageContextDto;

import java.util.List;

public interface EmailFilter {
    List<EmailMessageContextDto> filter(List<EmailMessageContextDto> emails);
}
