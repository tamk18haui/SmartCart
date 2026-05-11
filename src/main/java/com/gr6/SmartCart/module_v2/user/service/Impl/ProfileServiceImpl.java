package com.gr6.SmartCart.module_v2.user.service.Impl;

import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.module_v2.user.dto.ProfileDTO;
import com.gr6.SmartCart.module_v2.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    @Override
    public ProfileDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        return mapToDTO(user);
    }

    @Override
    public ProfileDTO updateProfile(String email, ProfileDTO profileDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // Chỉ cập nhật các trường được phép thay đổi
        user.setFullName(profileDTO.getFullName());
        user.setPhoneNumber(profileDTO.getPhoneNumber());
        user.setAvatarUrl(profileDTO.getAvatarUrl());

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    // Hàm phụ để chuyển đổi từ Entity sang DTO
    private ProfileDTO mapToDTO(User user) {
        return ProfileDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .build();
    }
}