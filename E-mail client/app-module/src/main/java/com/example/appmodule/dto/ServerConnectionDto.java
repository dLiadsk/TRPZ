package com.example.appmodule.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
public class ServerConnectionDto {
    private  String host;
     private int port;
     private String protocol;
}
