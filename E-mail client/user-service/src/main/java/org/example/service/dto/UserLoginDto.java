package org.example.service.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@RequiredArgsConstructor
@Jacksonized
public class UserLoginDto {
    private String username;
    private String password;
}
