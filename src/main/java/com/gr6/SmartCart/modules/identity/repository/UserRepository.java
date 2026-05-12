package com.gr6.SmartCart.modules.identity.repository;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.common.enums.UserStatus;
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

    // SÁNG THÊM: Lọc User cho Admin (theo Role, Status, Keyword)
    @Query("SELECT u FROM User u WHERE " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:keyword IS NULL OR :keyword = '' " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchForAdmin(@Param("role") UserRole role, @Param("status") UserStatus status, @Param("keyword") String keyword, Pageable pageable);
}