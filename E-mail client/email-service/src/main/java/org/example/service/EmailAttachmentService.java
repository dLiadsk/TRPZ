package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.dto.EmailAccountDto;
import org.example.service.dto.FileDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAttachmentService {
    private final RestTemplate restTemplate;
    private final String attachmentUrl = "http://localhost:8083/attachments";

    public List<FileDto> downloadAttachments(List<String> attachmentPaths) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<String>> requestEntity = new HttpEntity<>(attachmentPaths, headers);
            ResponseEntity<List<FileDto>> response = restTemplate.exchange(
                    attachmentUrl + "/getFiles",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getBody() == null) {
                log.error("Response body is null");
                throw new IllegalStateException("Failed to download attachments: response body is null");
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Error fetching attachments: {}", e.getResponseBodyAsString(), e);
            throw new IllegalArgumentException("Failed to fetch attachments: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Error during REST call: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to call attachment service: " + e.getMessage(), e);
        }
    }

}
