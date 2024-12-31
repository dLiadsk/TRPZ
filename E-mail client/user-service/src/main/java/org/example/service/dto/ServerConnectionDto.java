package org.example.service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder
public class ServerConnectionDto {
     String host;
     int port;
     String protocol;
}
