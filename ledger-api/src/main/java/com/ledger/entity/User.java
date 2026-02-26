package com.ledger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", indexes = {
    @Index(unique = true, name = "uk_phone", columnList = "phone"),
    @Index(unique = true, name = "uk_username", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 120)
    private String passwordHash;

    @Column(name = "create_time", nullable = false, updatable = false)
    private java.time.LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = java.time.LocalDateTime.now();
    }
}
