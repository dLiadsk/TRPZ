package com.example.appmodule.dto.email_message;

import com.example.appmodule.dto.email_account.EmailAccountDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
public class EmailMessageDeleteDto {
    private String messageId;
    private EmailAccountDto from;
}