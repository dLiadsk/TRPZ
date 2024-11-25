package org.example.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.model.common.Status;

import java.util.Date;
import java.util.List;

@Data
@RequiredArgsConstructor
public class EmailMessage {
    private Long id;
    private String subject;  // Тема листа
    private String from;  // Відправник
    private List<String> to;  // Список одержувачів
    private Date sentDate;  // Дата відправлення
    private String body;  // Тіло повідомлення
    private List<Attachment> attachments;  // Список вкладень
    private Status emailStatus;
}
