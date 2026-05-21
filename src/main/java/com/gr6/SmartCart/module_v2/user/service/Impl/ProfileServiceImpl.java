package com.gr6.SmartCart.module_v2.user.service.Impl;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.module_v2.user.dto.ProfileDTO;
import com.gr6.SmartCart.module_v2.user.service.ProfileService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        return mapToDTO(user);
    }

    @Override
    @Transactional
    public ProfileDTO updateProfile(String email, ProfileDTO profileDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (isBlank(profileDTO.getFullName())) {
            throw new RuntimeException("Họ tên không được để trống");
        }

        if (isBlank(profileDTO.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }

        user.setFullName(profileDTO.getFullName().trim());
        user.setPhoneNumber(profileDTO.getPhoneNumber().trim());
        user.setAvatarUrl(normalize(profileDTO.getAvatarUrl()));

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    private ProfileDTO mapToDTO(User user) {
        return ProfileDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole() == null ? null : user.getRole().name())
                .build();
    }

    private String normalize(String value) {
        if (value == null) return null;

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}