package com.example.appmodule.dto.email_account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
public class EmailAddressForFolderDto {
    private String folderName;
    private String address;
}
