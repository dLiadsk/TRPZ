package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Jacksonized
public class FileDto {
    private byte[] fileBytes;
    private String name;
    private String mimeType;
}
