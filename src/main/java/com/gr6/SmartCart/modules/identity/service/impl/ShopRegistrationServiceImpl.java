package com.gr6.SmartCart.modules.identity.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.common.enums.UserStatus;
import com.gr6.SmartCart.modules.identity.dto.ShopRegisterRequest;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.identity.service.ShopRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopRegistrationServiceImpl implements ShopRegistrationService {
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public BaseResponse<String> registerShop(ShopRegisterRequest request) {

        // 1. Kiểm tra xác nhận mật khẩu
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return BaseResponse.error(404,"Mật khẩu xác nhận không khớp!");
        }

        // 2. Check trùng Email
        if (userRepository.existsByEmail(request.getEmail())) {
            return BaseResponse.error(404,"Email này đã được sử dụng!");
        }

        // 3. Check trùng tên Shop
        if (shopRepository.existsByShopName(request.getShopName())) {
            return BaseResponse.error(404,"Tên shop đã tồn tại!");
        }

        // 4. Tạo User mới (Role SELLER)
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.SELLER);
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.save(user);

// 5. Tạo Shop (Trạng thái PENDING)
        Shop shop = new Shop();
        shop.setShopName(request.getShopName());
        shop.setPickupAddress(request.getPickupAddress());
        shop.setDescription(request.getDescription());
        shop.setStatus(ShopStatus.PENDING);
        shop.setUser(user); // Gán đối tượng user vừa lưu vào đây

        shopRepository.save(shop);

        return BaseResponse.successMessage("Đăng ký tài khoản người bán thành công! Vui lòng chờ phê duyệt.");
    }
}