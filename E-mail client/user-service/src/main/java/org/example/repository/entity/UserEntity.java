package org.example.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "\"user\"")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq")
    private Long id;
    private String username;

    @OneToMany(mappedBy = "user")
    private List<EmailAccountEntity> emailAccounts;

    private String password;
    private String phoneNumber;

    public UserEntity addEmailAccount(EmailAccountEntity entity){
        this.emailAccounts.add(entity);
        entity.setUser(this);
        return this;
    }
}

