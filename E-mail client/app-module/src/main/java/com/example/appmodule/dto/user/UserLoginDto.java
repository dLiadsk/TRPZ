package com.example.appmodule.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserLoginDto {
    private String username;
    private String password;
}
