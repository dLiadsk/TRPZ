package org.example.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class User {
    private Long id;
    private String username;
    private List<EmailAccount> emailAccounts;
    private String password;
    private String phoneNumber;
}
