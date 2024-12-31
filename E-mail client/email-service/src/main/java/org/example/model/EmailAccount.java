package org.example.model;

import jakarta.mail.Session;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.example.model.common.ProtocolType;

@Value
@Builder(toBuilder = true)
public class EmailAccount {
    String emailAddress;
     String password;
     ServerConnection incomingServer;
     ServerConnection outgoingServer;

     Session incomingServerSession;

     Session outgoingServerSession;
     Boolean autoconfig;

}