package org.example.service.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.example.model.EmailAccount;

import java.util.List;

@Value
@Builder
@Jacksonized
public class UserDto {
     Long id;
     String username;
     List<EmailAccountDto> emailAccounts;
     String password;
     String phoneNumber;
     String jwt;
}
