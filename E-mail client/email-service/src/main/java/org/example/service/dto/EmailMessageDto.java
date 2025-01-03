package org.example.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class EmailMessageDto {
    private String messageId;
    private String subject;  // Тема листа
    private String from;  // Відправник
    private List<String> to;  // Список одержувачів
    private LocalDateTime sentDate;  // Дата відправлення
    private String body;  // Тіло повідомлення
    private List<String> attachmentPaths;  // Список вкладень
    private String emailStatus;
}