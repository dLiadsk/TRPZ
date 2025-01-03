package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.EmailMessageContextDto;
import org.example.dto.MessagesToFolderDto;
import org.example.repository.*;
import org.example.repository.entity.*;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final FolderEmailAddressRepository folderEmailAddressRepository;

    private final FolderEmailMessageRepository folderEmailMessageRepository;
    private final EmailAddressRepository emailAddressRepository;
    private final EmailMessageRepository emailMessageRepository;


    @Transactional
    public void deleteMessageFromFolder(MessagesToFolderDto messages) {
        FolderEntity folder = folderRepository.findByName(messages.getFolderName())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Folder %s does not exist", messages.getFolderName())));

        EmailAddressEntity emailAddress = emailAddressRepository.findByAddress(messages.getEmailAddress())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Email address %s does not exist", messages.getEmailAddress())));

        FolderEmailAddressEntity folderEmailAddress = folderEmailAddressRepository.findByFolderAndEmailAddress(folder, emailAddress)
                .orElseThrow(() -> new IllegalArgumentException(String.format("User %s does not have folder %s", messages.getEmailAddress(), messages.getFolderName())));

        for (String messageId : messages.getMessageIds()) {
            EmailMessageEntity entity = emailMessageRepository.findById(messageId)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Email message with id %s does not exist", messageId)));
            folderEmailMessageRepository.deleteByFolderEmailAddressAndMessage(folderEmailAddress, entity);
        }
    }

    @Transactional
    public void saveMessagesToFolder(MessagesToFolderDto dto) throws ChangeSetPersister.NotFoundException {
        FolderEntity folder = folderRepository.findByName(dto.getFolderName())
                .orElseGet(() -> {
                    FolderEntity newFolder = new FolderEntity();
                    newFolder.setName(dto.getFolderName());
                    return folderRepository.save(newFolder);
                });
        EmailAddressEntity emailAddress = emailAddressRepository.findByAddress(dto.getEmailAddress())
                .orElseGet(() -> {
                    EmailAddressEntity entity = new EmailAddressEntity();
                    entity.setAddress(dto.getEmailAddress());
                    return emailAddressRepository.save(entity);
                });

        FolderEmailAddressEntity folderEmailAddress = folderEmailAddressRepository.findByFolderAndEmailAddress(folder, emailAddress)
                .orElseGet(() -> {
                    FolderEmailAddressEntity newFolderEmail = new FolderEmailAddressEntity();
                    newFolderEmail.setFolder(folder);
                    newFolderEmail.setEmailAddress(emailAddress);
                    return folderEmailAddressRepository.save(newFolderEmail);
                });
        for (String messageId : dto.getMessageIds()) {
            EmailMessageEntity entity = emailMessageRepository.findById(messageId).orElseThrow(ChangeSetPersister.NotFoundException::new);
            if (!folderEmailMessageRepository.existsByFolderEmailAddressAndMessage(folderEmailAddress, entity)) {
                FolderEmailMessageEntity folderEmailMessage = new FolderEmailMessageEntity();
                folderEmailMessage.setFolderEmailAddress(folderEmailAddress);
                folderEmailMessage.setMessage(entity);
                folderEmailMessageRepository.save(folderEmailMessage);
            }
        }
    }

    @Transactional
    public void addFolderWithEmail(String emailAddress, String folderName) throws ChangeSetPersister.NotFoundException {
        // Знайти або створити папку
        FolderEntity folderEntity = folderRepository.findByName(folderName)
                .orElseGet(() -> {
                    FolderEntity newFolder = new FolderEntity();
                    newFolder.setName(folderName);
                    return folderRepository.save(newFolder);
                });

        boolean emailExists = folderEntity.getFolderEmailAddresses().stream()
                .map(FolderEmailAddressEntity::getEmailAddress)
                .anyMatch(email -> email.getAddress().equals(emailAddress));

        if (!emailExists) {
            EmailAddressEntity emailEntity = emailAddressRepository.findByAddress(emailAddress)
                    .orElseThrow(ChangeSetPersister.NotFoundException::new);

            FolderEmailAddressEntity folderEmailAddressEntity = new FolderEmailAddressEntity();
            folderEmailAddressEntity.setFolder(folderEntity);
            folderEmailAddressEntity.setEmailAddress(emailEntity);

            folderEntity.getFolderEmailAddresses().add(folderEmailAddressEntity);
            folderRepository.save(folderEntity);
        }
    }

    public List<String> getFolders(String email) {
        return folderRepository.findFoldersByEmailAddress(email).stream().map(FolderEntity::getName).toList();
    }


    public List<EmailMessageContextDto> getEmailsFromFolder(String email, String folderName) {
        EmailAddressEntity emailAddressEntity = emailAddressRepository.findByAddress(email).orElseGet(() -> {
            EmailAddressEntity entity = new EmailAddressEntity();
            entity.setAddress(email);
            return emailAddressRepository.save(entity);
        });
        FolderEntity folder = folderRepository.findByName(folderName)
                .orElseGet(() -> {
                    FolderEntity newFolder = new FolderEntity();
                    newFolder.setName(folderName);
                    return folderRepository.save(newFolder);
                });
        FolderEmailAddressEntity folderEmailAddressEntity = folderEmailAddressRepository.findByFolderAndEmailAddress(folder, emailAddressEntity).orElseGet(() -> {
            FolderEmailAddressEntity newFolderEmail = new FolderEmailAddressEntity();
            newFolderEmail.setFolder(folder);
            newFolderEmail.setEmailAddress(emailAddressEntity);
            return folderEmailAddressRepository.save(newFolderEmail);
        });
        List<FolderEmailMessageEntity> folderEmailMessageEntity = folderEmailMessageRepository.findFolderEmailMessageEntitiesByFolderEmailAddress(folderEmailAddressEntity);
        List<EmailMessageEntity> messages = folderEmailMessageEntity.stream().map(FolderEmailMessageEntity::getMessage).toList();
        return mapToEmailContextDto(messages);
    }

    private List<EmailMessageContextDto> mapToEmailContextDto(List<EmailMessageEntity> entities) {
        List<EmailMessageContextDto> emails = new ArrayList<>();
        for (EmailMessageEntity entity : entities) {
            EmailMessageContextDto contextDto = new EmailMessageContextDto();
            contextDto.setEmailStatus(entity.getEmailStatus().name());
            contextDto.setId(entity.getMessageId());
            contextDto.setFrom(entity.getFrom());
            contextDto.setSubject(entity.getSubject());
            contextDto.setSentDate(entity.getSentDate());
            emails.add(contextDto);
        }
        return emails;
    }
}
