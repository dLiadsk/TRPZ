package org.example.service.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import javax.crypto.SecretKey;

@Data
@RequiredArgsConstructor
@Jacksonized
public class UserLoginDto {
    private String username;
    private String password;
    private String key;

}
