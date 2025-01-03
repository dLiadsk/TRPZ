package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
public class FilterDto {
    private List<EmailMessageContextDto> emails;
    private String status;
    private String sender;
    private String subject;
    private LocalDateTime from;
    private LocalDateTime to;
}
