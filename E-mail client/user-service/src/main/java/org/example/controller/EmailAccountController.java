package org.example.controller;

import org.example.service.EmailAccountService;
import org.example.service.UserService;
import org.example.service.dto.EmailAccountDto;
import org.example.service.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails")
public class EmailAccountController {
    private final EmailAccountService emailAccountService;
    private final UserService userService;


    public EmailAccountController(EmailAccountService emailAccountService, UserService userService) {
        this.emailAccountService = emailAccountService;
        this.userService = userService;
    }
    @PostMapping("/{username}")
    public ResponseEntity<UserDto> createEmail(@RequestBody EmailAccountDto emailAccountDto, @PathVariable("username") String username) {
        try {
            emailAccountService.addEmailAccount(username, emailAccountDto);
            UserDto userDto = userService.gerUserByUsername(username);
            return new ResponseEntity<>(userDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
