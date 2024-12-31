package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.FileDto;
import org.example.service.AttachmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/getFiles")
    public ResponseEntity<List<FileDto>> uploadAttachments(@RequestBody List<String> attachmentPaths) {
        try {
            List<FileDto> response = attachmentService.getAttachments(attachmentPaths);
            return ResponseEntity.ok(response);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}

