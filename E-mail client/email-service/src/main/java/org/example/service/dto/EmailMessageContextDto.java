package org.example.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
public class EmailMessageContextDto {
    private String id;
    private String subject;  // Тема листа
    private String from;  // Відправник
    private LocalDateTime sentDate;  // Дата відправлення
    private String emailStatus;
}

