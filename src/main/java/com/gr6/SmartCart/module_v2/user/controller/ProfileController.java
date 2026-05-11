package com.gr6.SmartCart.module_v2.user.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v2.user.dto.ProfileDTO;
import com.gr6.SmartCart.module_v2.user.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/user/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<BaseResponse<ProfileDTO>> getProfile(Authentication authentication) {
        String email = authentication.getName(); // Lấy email từ Token
        ProfileDTO profile = profileService.getMyProfile(email);
        return ResponseEntity.ok(BaseResponse.success_data("Lấy thông tin thành công", profile));
    }

    @PutMapping
    public ResponseEntity<BaseResponse<ProfileDTO>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileDTO profileDTO) {

        String email = authentication.getName();
        ProfileDTO updatedProfile = profileService.updateProfile(email, profileDTO);
        return ResponseEntity.ok(BaseResponse.success_data("Cập nhật thông tin thành công", updatedProfile));
    }
}