package org.example.model;

import jakarta.mail.Flags;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Date;
import java.util.List;

@Data
@RequiredArgsConstructor
public class EmailMessage {
    private String id;
    private String subject;  // Тема листа
    private String from;  // Відправник
    private List<String> to;  // Список одержувачів
    private Date sentDate;  // Дата відправлення
    private String body;  // Тіло повідомлення
    private List<File> attachments;  // Список вкладень
    private Flags.Flag emailStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailMessage that = (EmailMessage) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
