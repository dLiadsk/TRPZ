package com.example.appmodule.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UserLoginDto {
    private String username;
    private String password;
    private String key;

}
