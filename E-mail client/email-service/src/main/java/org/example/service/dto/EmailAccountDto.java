package org.example.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
@Builder
public class EmailAccountDto {
    private String emailAddress;
    private String password;
    private ServerConnectionDto incomingServer;
    private ServerConnectionDto outgoingServer;
    private Boolean autoconfig;

    @Override
    public String toString() {
        return emailAddress;
    }
}
