package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.service.EmailAddressService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email/address")
@RequiredArgsConstructor
public class EmailAddressController {
    private final EmailAddressService emailAddressService;

    @PostMapping("/save/email/address")
    public ResponseEntity<Void> saveEmailAddress(@RequestBody String emailAddress){
        try {
            emailAddressService.saveAddress(emailAddress);
            return ResponseEntity.ok().build();
        }catch (ChangeSetPersister.NotFoundException e){
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }
}
