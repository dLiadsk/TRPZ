package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.EmailMessageContextDto;
import org.example.dto.FilterDto;
import org.example.service.EmailFilterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/filter")
@RequiredArgsConstructor
public class FilterController {
    private final EmailFilterService emailFilterService;

    @PostMapping
    public ResponseEntity<List<EmailMessageContextDto>> filter(@RequestBody FilterDto filterDto){
        List<EmailMessageContextDto> emails = emailFilterService.filter(filterDto).stream()
                .sorted(Comparator.comparing(EmailMessageContextDto::getSentDate))
                .collect(Collectors.toList());
        return ResponseEntity.ok(emails);
    }
}
