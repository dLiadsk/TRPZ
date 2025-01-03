package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.example.model.common.ProtocolType;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ServerConnection {
     private String host;
     private int port;
     private ProtocolType protocol;
}