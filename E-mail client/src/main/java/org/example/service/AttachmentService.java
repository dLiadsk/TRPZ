package org.example.service;

import org.example.model.Attachment;
import org.example.repository.AttachmentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }


    public void downloadAttachment(Attachment attachment, String downloadPath) throws IOException {
        Path path = Paths.get(downloadPath, attachment.getFileName());
        Files.write(path, attachment.getData());
        System.out.println("Attachment downloaded to: " + path.toString());
    }

    public Attachment addAttachment(Attachment attachment) throws SQLException {
        return attachmentRepository.save(attachment);
    }
}
