package com.example.appmodule.config;

import com.example.appmodule.dto.email.EmailMessageContextDto;
import com.example.appmodule.dto.user.UserDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class AuthenticatedUser {
    private static AuthenticatedUser instance;
    private UserDto user;
    private List<EmailMessageContextDto> emails;

    private AuthenticatedUser() {
    }

    public static AuthenticatedUser getInstance() {
        if (instance == null) {
            instance = new AuthenticatedUser();
        }
        return instance;
    }
}