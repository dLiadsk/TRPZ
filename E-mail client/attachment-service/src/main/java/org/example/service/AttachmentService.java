package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.FileDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class AttachmentService {
    public final String FOLDER = "attachments";
    public List<FileDto> getAttachments(List<String> attachmentPaths) throws IOException {
        List<FileDto> attachments = new ArrayList<>();

        for (String path : attachmentPaths) {
            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
                throw new FileNotFoundException("File not found: " + path);
            }
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // За замовчуванням, якщо MIME-тип не визначено
            }
            attachments.add(new FileDto(Files.readAllBytes(file.toPath()), file.getName(), mimeType));
        }

        return attachments;
    }

    public List<String> downloadAttachments(List<FileDto> files) throws IOException {
        List<String> filePaths = new ArrayList<>();
        // Перевіряємо, чи існує папка, і створюємо її, якщо потрібно
        File folder = new File(FOLDER);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IOException("Failed to create directory: " + FOLDER);
            }
        }
        // Проходимося по списку файлів
        for (FileDto fileDto : files) {
            // Створюємо шлях до нового файлу
            File file = new File(FOLDER, fileDto.getName());
            // Записуємо байти у файл
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(fileDto.getFileBytes());
                filePaths.add(file.getAbsolutePath());
            } catch (IOException e) {
                throw new IOException("Error writing file: " + fileDto.getName(), e);
            }
        }
        return filePaths;
    }

}

