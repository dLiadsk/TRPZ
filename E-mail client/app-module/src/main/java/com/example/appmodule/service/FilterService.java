package com.example.appmodule.service;

import com.example.appmodule.dto.email_message.EmailMessageContextDto;
import com.example.appmodule.dto.email_message.FilterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FilterService {
    private final RestTemplate restTemplate;
    private final String filterControllerUrl = "http://localhost:8084/filter";

    public FilterService() {
        this.restTemplate = new RestTemplate();
    }

    public List<EmailMessageContextDto> filter(List<EmailMessageContextDto> emailMessageContextDto, String status, String sender, String subject, LocalDateTime from, LocalDateTime to) {
        if (emailMessageContextDto.isEmpty()) {
            return new ArrayList<>();
        }
        FilterDto filterDto = new FilterDto(emailMessageContextDto, status, sender, subject, from, to);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<FilterDto> requestEntity = new HttpEntity<>(filterDto, headers);
        ResponseEntity<List<EmailMessageContextDto>> response = restTemplate.exchange(
                filterControllerUrl,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else throw new IllegalArgumentException();
    }
}
