package com.example.appmodule.service;

import com.example.appmodule.dto.email.EmailAccountDto;
import com.example.appmodule.dto.ServerConnectionDto;
import com.example.appmodule.dto.user.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class EmailAccountService {
    private final RestTemplate restTemplate;

    public EmailAccountService() {
        this.restTemplate = new RestTemplate();
    }

    private final String emailAccountControllerUrl = "http://localhost:8081/emails";
    private final String folderServiceControllerUrl = "http://localhost:8085/email/address";


    public UserDto addEmailAccount(String emailAddress, String password, Boolean autoconfig, ServerConnectionDto in, ServerConnectionDto out, UserDto userDto){
        if (!autoconfig){
            String domain = emailAddress.split("@")[1];
            in.setHost(in.getProtocol() + "." + domain);
            out.setHost(out.getProtocol() + "." + domain);
        }
        EmailAccountDto emailAccountDto = new EmailAccountDto(emailAddress, password, in, out, autoconfig);

        try {
            ResponseEntity<UserDto> response = restTemplate.postForEntity(
                    emailAccountControllerUrl + "/" + userDto.getUsername(),
                    emailAccountDto,
                    UserDto.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Failed to add email account. Status code: " + response.getStatusCode());
            }

            ResponseEntity<Void> response1 = restTemplate.postForEntity(
                    folderServiceControllerUrl + "/save/email/address",
                    emailAccountDto.getEmailAddress(),
                    Void.class
            );

            if (!response1.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Failed to save email address. Status code: " + response1.getStatusCode());
            }

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("Error adding email: " + e.getResponseBodyAsString());
            throw new IllegalArgumentException("Email adding email failed: " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred while adding email", e);
        }
    }
}
