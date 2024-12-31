package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.EmailAddressForFolderDto;
import org.example.dto.EmailMessageContextDto;
import org.example.dto.MessagesToFolderDto;
import org.example.service.FolderService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<Void> createFolder(@RequestBody EmailAddressForFolderDto emailAddress) {
        try {
            folderService.addFolderWithEmail(emailAddress.getAddress().trim(), emailAddress.getFolderName().trim());
            return ResponseEntity.ok().build();
        } catch (ChangeSetPersister.NotFoundException e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/save/messages")
    public ResponseEntity<Void> saveEmails(@RequestBody MessagesToFolderDto messages) {
        try {
            if (!messages.getFolderName().equals("Trash")){
                messages.setFolderName("All");
                folderService.saveMessagesToFolder(messages);
            }
            folderService.saveMessagesToFolder(messages);
            return ResponseEntity.ok().build();
        } catch (ChangeSetPersister.NotFoundException e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }
    @PostMapping("/get/folders")
    public ResponseEntity<List<String>> getFolders(@RequestBody String email){
        List<String> folders = folderService.getFolders(email);
        return ResponseEntity.ok(folders);
    }
    @PostMapping("/get/emails/from/folder")
    public ResponseEntity<List<EmailMessageContextDto>> getMessagesFromFolder(@RequestBody EmailAddressForFolderDto email){
        List<EmailMessageContextDto> emails = folderService.getEmailsFromFolder(email.getAddress(), email.getFolderName());
        return ResponseEntity.ok(emails);
    }
    @DeleteMapping("/delete/from/folder")
    public ResponseEntity<Void> deleteFromFolder(@RequestBody MessagesToFolderDto messages){
        folderService.deleteMessageFromFolder(messages);
        return ResponseEntity.ok().build();
    }
}
