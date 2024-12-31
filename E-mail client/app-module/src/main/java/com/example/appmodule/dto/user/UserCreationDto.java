package com.example.appmodule.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
public class UserCreationDto {
    private String username;
    private String password;
    private String phoneNumber;
}
