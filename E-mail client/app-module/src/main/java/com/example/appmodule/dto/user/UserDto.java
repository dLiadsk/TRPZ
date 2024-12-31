package com.example.appmodule.dto.user;

import com.example.appmodule.dto.email.EmailAccountDto;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
public class UserDto {
     private Long id;
     private String username;
     private List<EmailAccountDto> emailAccounts;
     private String password;
     private String phoneNumber;
}
