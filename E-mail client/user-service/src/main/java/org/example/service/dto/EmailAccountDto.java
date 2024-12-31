package org.example.service.dto;

import jakarta.mail.Session;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.example.model.ServerConnection;

@Value
@Builder
@Jacksonized
public class EmailAccountDto {
     String emailAddress;
     String password;
     ServerConnectionDto incomingServer;
     ServerConnectionDto outgoingServer;
     Boolean autoconfig;
}
