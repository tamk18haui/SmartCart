package com.gr6.SmartCart.module_v2.user.service;

import com.gr6.SmartCart.module_v2.user.dto.ProfileDTO;

public interface ProfileService {
    // Lấy thông tin cá nhân của người đang đăng nhập
    ProfileDTO getMyProfile(String email);

    // Cập nhật thông tin cá nhân
    ProfileDTO updateProfile(String email, ProfileDTO profileDTO);
}