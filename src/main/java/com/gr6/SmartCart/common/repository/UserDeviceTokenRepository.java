package com.gr6.SmartCart.common.repository;

import com.gr6.SmartCart.common.domain.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    List<UserDeviceToken> findByUser_UserIdAndActiveTrue(Long userId);

    Optional<UserDeviceToken> findByFcmToken(String fcmToken);

    Optional<UserDeviceToken> findByUser_UserIdAndDeviceId(Long userId, String deviceId);
}
