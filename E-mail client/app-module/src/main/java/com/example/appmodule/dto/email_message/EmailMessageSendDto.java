package com.example.appmodule.dto.email_message;

import com.example.appmodule.dto.email_account.EmailAccountDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
public class EmailMessageSendDto {
    private String subject;  // Тема листа
    private EmailAccountDto from;
    private List<String> to;  // Список одержувачів
    private String body;  // Тіло повідомлення
    private List<String> attachmentPaths;  // Список вкладень
}
