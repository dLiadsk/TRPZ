package org.example.service.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import javax.crypto.SecretKey;

@Data
@RequiredArgsConstructor
@Jacksonized
public class UserCreationDto {
    private String username;
    private String password;
    private String phoneNumber;
    private String key;

}
