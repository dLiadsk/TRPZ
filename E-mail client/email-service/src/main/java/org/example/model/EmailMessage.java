package org.example.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.model.common.EmailStatus;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class EmailMessage {
    private String messageId;
    private String subject;  // Тема листа
    private String from;  // Відправник
    private List<String> to;  // Список одержувачів
    private LocalDateTime sentDate;  // Дата відправлення
    private String body;  // Тіло повідомлення
    private List<String> attachmentNames;  // Список вкладень
    private EmailStatus emailStatus;
}
