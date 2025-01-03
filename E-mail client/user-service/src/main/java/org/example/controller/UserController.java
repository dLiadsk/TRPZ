package org.example.controller;

import org.example.model.User;
import org.example.service.UserService;
import org.example.service.dto.EmailAccountDto;
import org.example.service.dto.UserCreationDto;
import org.example.service.dto.UserDto;
import org.example.service.dto.UserLoginDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreationDto user) {
        try {
            UserDto createdUser = userService.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody UserLoginDto user) {
        try {
            UserDto loginUser = userService.login(user.getUsername(), user.getPassword(), user.getKey());
            return new ResponseEntity<>(loginUser, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{username}")
    public ResponseEntity<List<EmailAccountDto>> getEmails(@PathVariable(name = "username") String username) {
        try {
            return new ResponseEntity<>(userService.gerUserEmails(username), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

}

