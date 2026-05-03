package com.gr6.SmartCart.common.domain;

import com.gr6.SmartCart.common.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "Users")
@Data @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String avatarUrl;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private UserRole role;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private UserStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Shop shop;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Wallet wallet;
}