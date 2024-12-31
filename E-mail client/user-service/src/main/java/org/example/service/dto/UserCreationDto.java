package org.example.service.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@RequiredArgsConstructor
@Jacksonized
public class UserCreationDto {
    private String username;
    private String password;
    private String phoneNumber;
}
