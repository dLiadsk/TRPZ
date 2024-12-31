package org.example.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.EmailAccount;
import org.example.model.EmailMessage;
import org.example.service.EmailMessageService;
import org.example.service.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailMessageService emailMessageService;


    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailMessageSendDto emailMessageSendDto) {
        try {
            emailMessageService.sendEmail(emailMessageSendDto);
            return ResponseEntity.ok("Email sent successfully");
        } catch (MessagingException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/save/draft")
    public ResponseEntity<String> saveDraft(@RequestBody EmailMessageSendDto emailMessageSendDto) {
        try {
            emailMessageService.saveDraft(emailMessageSendDto);
            return ResponseEntity.ok("Draft saved successfully");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save draft: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<List<EmailMessageContextDto>> getEmails(@RequestBody List<EmailAccountDto> emailAccounts) throws SQLException {
        try {
            List<EmailMessageContextDto> emailMessages = emailMessageService.getEmailMessages(emailAccounts);
            return ResponseEntity.ok(emailMessages);
        } catch (MessagingException | IOException e) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/get/message")
    public ResponseEntity<EmailMessageDto> getEmail(@RequestBody EmailMessageContextDto contextDto) {
        EmailMessageDto messageDto = emailMessageService.getEmailMessage(contextDto);
        return ResponseEntity.ok(messageDto);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteEmail(@RequestBody EmailMessageDeleteDto emailMessageDeleteDto) throws MessagingException {
            emailMessageService.deleteEmailMessage(emailMessageDeleteDto);
            return ResponseEntity.ok().build();

    }
}
