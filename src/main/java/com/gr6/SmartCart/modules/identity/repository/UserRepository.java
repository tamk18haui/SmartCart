package com.gr6.SmartCart.modules.identity.repository;

import com.gr6.SmartCart.common.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Lấy danh sách User có Role là BUYER, hỗ trợ search theo Tên hoặc Email
    @Query("SELECT u FROM User u WHERE u.role = 'BUYER' " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchBuyers(@Param("keyword") String keyword, Pageable pageable);
}