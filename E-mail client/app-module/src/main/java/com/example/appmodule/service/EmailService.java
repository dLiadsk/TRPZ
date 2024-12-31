package com.example.appmodule.service;

import com.example.appmodule.dto.email.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailService {
    private final RestTemplate restTemplate;
    private final String emailControllerUrl = "http://localhost:8082/emails";

    public EmailService() {
        this.restTemplate = new RestTemplate();
    }

    public List<EmailMessageContextDto> getAllMessages(List<EmailAccountDto> emailAccounts) {
        if (emailAccounts.isEmpty()) {
            return new ArrayList<>();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<EmailAccountDto>> requestEntity = new HttpEntity<>(emailAccounts, headers);
        ResponseEntity<List<EmailMessageContextDto>> response = restTemplate.exchange(
                emailControllerUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else throw new IllegalArgumentException();
    }
    public void deleteMessage( EmailAccountDto emailAccountDto, String messageId) {
        EmailMessageDeleteDto emailMessageSendDto = new EmailMessageDeleteDto(messageId, emailAccountDto);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmailMessageDeleteDto> requestEntity = new HttpEntity<>(emailMessageSendDto, headers);

        restTemplate.exchange(
                emailControllerUrl,
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );
    }

    public EmailMessageDto getMessage(EmailMessageContextDto email) {
        try {
            ResponseEntity<EmailMessageDto> response = restTemplate.postForEntity(emailControllerUrl + "/get/message", email, EmailMessageDto.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("Error get message: " + e.getResponseBodyAsString());
            throw new IllegalArgumentException("Getting message failed: " + e.getMessage());
        }
    }

    public String sendMessage(String subject, EmailAccountDto from, List<String> to, String body, List<String> attachmentNamePaths) {
        try {
            EmailMessageSendDto messageSendDto = new EmailMessageSendDto(subject, from, to, body, attachmentNamePaths);
            ResponseEntity<String> response = restTemplate.postForEntity(emailControllerUrl + "/send", messageSendDto, String.class);
            return response.getBody();

        } catch (HttpClientErrorException e) {
            System.out.println("Error send message: " + e.getResponseBodyAsString());
            throw new IllegalArgumentException("Sending message failed: " + e.getMessage(), e);
        }

    }

    public String saveDraft(String subject, EmailAccountDto from, List<String> to, String body, List<String> attachmentNamePaths) {
        try {
            EmailMessageSendDto messageSendDto = new EmailMessageSendDto(subject, from, to, body, attachmentNamePaths);
            ResponseEntity<String> response = restTemplate.postForEntity(emailControllerUrl + "/save/draft", messageSendDto, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println("Error saving draft: " + e.getResponseBodyAsString());
            throw new IllegalArgumentException("Saving draft failed: " + e.getMessage(), e);
        }
    }
}
