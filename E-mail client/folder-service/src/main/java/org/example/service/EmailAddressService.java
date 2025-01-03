package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.repository.EmailAddressRepository;
import org.example.repository.entity.EmailAddressEntity;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailAddressService {
    private final EmailAddressRepository emailAddressRepository;
    public void saveAddress(String address) throws ChangeSetPersister.NotFoundException {
        Optional<EmailAddressEntity> existingAddress = emailAddressRepository.findByAddress(address);
        if (existingAddress.isPresent()) {
            log.info("Email address {} already exists.", address);
            return;
        }

        EmailAddressEntity newAddress = new EmailAddressEntity();
        newAddress.setAddress(address);
        emailAddressRepository.save(newAddress);
    }
}
