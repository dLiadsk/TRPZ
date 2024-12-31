package com.example.appmodule.service;

import com.example.appmodule.dto.email.EmailAccountDto;
import com.example.appmodule.dto.user.UserCreationDto;
import com.example.appmodule.dto.user.UserDto;
import com.example.appmodule.dto.user.UserLoginDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Component
@RequiredArgsConstructor
public class UserService {
    private final RestTemplate restTemplate;

    public UserService() {
        this.restTemplate = new RestTemplate();
    }

    private final String userControllerUrl = "http://localhost:8081/users";

    public UserDto singUp(String username, String password, String phoneNumber){
        UserCreationDto user = new UserCreationDto(username, password, phoneNumber);
        try {
            ResponseEntity<UserDto> response = restTemplate.postForEntity(userControllerUrl, user, UserDto.class);
            return response.getBody();
        }
        catch (HttpClientErrorException e) {
            System.out.println("Error creating user: " + e.getResponseBodyAsString());
            throw new IllegalArgumentException("User creation failed: " + e.getMessage());
        }
    }
    public UserDto login(String username, String password){
        UserLoginDto userLoginDto = new UserLoginDto(username, password);
        try {
            ResponseEntity<UserDto> response = restTemplate.postForEntity(userControllerUrl + "/login", userLoginDto, UserDto.class);
            return response.getBody();
        }
        catch (HttpClientErrorException e) {
            System.out.println("Error login user: " + e.getResponseBodyAsString());
            throw new IllegalArgumentException("User login failed: " + e.getMessage());
        }
    }
    public List<EmailAccountDto> getMyEmailsList(String username){
        try {
            ResponseEntity<List<EmailAccountDto>> response = restTemplate.exchange(
                    userControllerUrl + "/" + username,
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
            return response.getBody();
        }
        catch (HttpClientErrorException e) {
            System.err.println("Error fetching email list: " + e.getResponseBodyAsString());
            throw new IllegalArgumentException("Failed to fetch email list: " + e.getMessage());
        } catch (RestClientException e) {
            System.err.println("Error during REST call: " + e.getMessage());
            throw new IllegalStateException("Failed to call user controller: " + e.getMessage());
        }
    }

}
