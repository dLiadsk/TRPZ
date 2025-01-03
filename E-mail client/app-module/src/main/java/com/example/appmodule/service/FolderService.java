package com.example.appmodule.service;

import com.example.appmodule.dto.email_account.EmailAddressForFolderDto;
import com.example.appmodule.dto.email_message.EmailMessageContextDto;
import com.example.appmodule.dto.email_message.EmailMessageDto;
import com.example.appmodule.dto.email_message.MessagesToFolderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FolderService {
    private final RestTemplate restTemplate;
    private final String folderControllerUrl = "http://localhost:8085/folders";

    public FolderService() {
        this.restTemplate = new RestTemplate();
    }

    public List<String> getFolders(String emailAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(emailAddress, headers);
        ResponseEntity<List<String>> response = restTemplate.exchange(
                folderControllerUrl + "/get/folders",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else throw new IllegalArgumentException();
    }

    public List<String> createFolder(String folderName, String emailAddress) {
        EmailAddressForFolderDto email = new EmailAddressForFolderDto(folderName, emailAddress);

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(folderControllerUrl, email, Void.class);
        } catch (HttpClientErrorException e) {
            System.out.println("Error get message: " + e.getResponseBodyAsString());
            throw new IllegalArgumentException("Getting message failed: " + e.getMessage());
        }
        return getFolders(email.getAddress());
    }

    public List<EmailMessageContextDto> getMessagesFromFolder(String folderName, String emailAddress) {
        EmailAddressForFolderDto email = new EmailAddressForFolderDto(folderName, emailAddress);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EmailAddressForFolderDto> requestEntity = new HttpEntity<>(email, headers);
        ResponseEntity<List<EmailMessageContextDto>> response = restTemplate.exchange(
                folderControllerUrl + "/get/emails/from/folder",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else throw new IllegalArgumentException();
    }
    public void addMessageToFolder(EmailMessageDto message, String email, String folder){
        MessagesToFolderDto messages = new MessagesToFolderDto();
        List<String> id = new ArrayList<>(Collections.singleton(message.getMessageId()));
        messages.setMessageIds(id);
        messages.setFolderName(folder);
        messages.setEmailAddress(email);
        ResponseEntity<Void> response = restTemplate.postForEntity(folderControllerUrl + "/save/messages", messages, Void.class);
        if (!response.getStatusCode().is2xxSuccessful()){
            throw new IllegalArgumentException("Failed to save emails. Status code: " + response.getStatusCode());
        }
    }
    public void deleteMessageFromFolder(String messageId, String emailAddress, String folderName){
        MessagesToFolderDto messagesDeleteFromInbox = new MessagesToFolderDto(List.of(messageId), emailAddress, folderName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MessagesToFolderDto> requestEntity = new HttpEntity<>(messagesDeleteFromInbox, headers);
        restTemplate.exchange(
                folderControllerUrl + "/delete/from/folder",
                HttpMethod.DELETE,
                requestEntity,
                Void.class
        );
    }
}
