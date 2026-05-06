package com.gr6.SmartCart.common.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity @Table(name = "User_Voucher_Usages")
@Getter
@Setter
public class UserVoucherUsage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    private Integer usedCount; // Mặc định mỗi người được 1 lần, hoặc cấu hình sau

    @CreationTimestamp
    private LocalDateTime lastUsedAt;
}