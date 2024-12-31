package com.example.appmodule.dto.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
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
        return from + ": " + subject;
    }
}

