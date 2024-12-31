package com.example.appmodule.dto.email;

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