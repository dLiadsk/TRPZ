package org.example.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@RequiredArgsConstructor
public class Attachment {
    private String fileName;
    private byte[] data;  // Дані файлу
    private String contentType;  // Тип контенту файлу (наприклад, image/jpeg, application/pdf)


}
