package com.example.appmodule.dto.email_account;

import com.example.appmodule.dto.ServerConnectionDto;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Jacksonized
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
