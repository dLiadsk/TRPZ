package org.example.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Attachment {
    private String fileName;
    private byte[] data;  // Дані файлу
    private String contentType;  // Тип контенту файлу (наприклад, image/jpeg, application/pdf)
}
