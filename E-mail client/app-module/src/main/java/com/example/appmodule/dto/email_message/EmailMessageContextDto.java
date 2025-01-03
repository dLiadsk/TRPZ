package com.example.appmodule.dto.email_message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @Override
    public String toString() {
        if (sentDate.getDayOfYear() == LocalDateTime.now().getDayOfYear())
            return sentDate.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "    " + from + ": " + subject;

        return sentDate.toLocalDate() + "    " + from + ": " + subject;
    }
}

