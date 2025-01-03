package com.example.appmodule.dto.email_message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
public class MessagesToFolderDto {
    List<String> messageIds;
    String emailAddress;
    String folderName;
}
