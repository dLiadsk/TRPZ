package com.example.appmodule.service;

import com.example.appmodule.config.AES;
import com.example.appmodule.dto.email_account.EmailAccountDto;
import com.example.appmodule.dto.user.UserCreationDto;
import com.example.appmodule.dto.user.UserDto;
import com.example.appmodule.dto.user.UserLoginDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;


@Component
@RequiredArgsConstructor
public class UserService {
    private final RestTemplate restTemplate;

    public UserService() {
        this.restTemplate = new RestTemplate();
    }

    private final String userControllerUrl = "http://localhost:8081/users";

    public UserDto singUp(String username, String password, String phoneNumber) throws Exception {
        AES aes = new AES();

        UserCreationDto user = new UserCreationDto(aes.encrypt(username), aes.encrypt(password), aes.encrypt(phoneNumber), Base64.getEncoder().encodeToString(aes.getSecretKey().getEncoded()));
        try {
            ResponseEntity<UserDto> response = restTemplate.postForEntity(userControllerUrl, user, UserDto.class);
            UserDto enc = response.getBody();
            UserDto userDto = new UserDto(enc.getId(), aes.decrypt(enc.getUsername()), enc.getEmailAccounts(), aes.decrypt(enc.getPassword()), aes.decrypt(enc.getPhoneNumber()), enc.getJwt());
            System.out.println(userDto.getUsername());
            System.out.println(userDto.getJwt());
            return userDto;
        }
        catch (HttpClientErrorException e) {
            System.out.println("Error creating user: " + e.getResponseBodyAsString());
            throw new IllegalArgumentException("User creation failed: " + e.getMessage());
        }
    }
    public UserDto login(String username, String password) throws Exception {
        AES aes = new AES();

        UserLoginDto userLoginDto = new UserLoginDto(aes.encrypt(username), aes.encrypt(password), Base64.getEncoder().encodeToString(aes.getSecretKey().getEncoded()));

        try {
            ResponseEntity<UserDto> response = restTemplate.postForEntity(userControllerUrl + "/login", userLoginDto, UserDto.class);
            UserDto enc = response.getBody();
            UserDto userDto = new UserDto(enc.getId(), aes.decrypt(enc.getUsername()), enc.getEmailAccounts(), aes.decrypt(enc.getPassword()), aes.decrypt(enc.getPhoneNumber()), enc.getJwt());
            System.out.println(userDto.getUsername());
            System.out.println(userDto.getJwt());
            return userDto;
        }
        catch (HttpClientErrorException e) {
            System.out.println("Error login user: " + e.getResponseBodyAsString());
            e.printStackTrace();
            throw new IllegalArgumentException("User login failed: " + e.getMessage());
        }
    }


}
