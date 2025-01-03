package org.example.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "email_addresses")
@Data
public class EmailAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_addresses_id_seq")
    @SequenceGenerator(name = "email_addresses_id_seq", sequenceName = "email_addresses_id_seq")
    private Long id;

    @Column(unique = true, nullable = false)
    private String address;
}
