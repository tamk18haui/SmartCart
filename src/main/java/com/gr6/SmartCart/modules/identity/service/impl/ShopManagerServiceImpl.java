package com.gr6.SmartCart.modules.identity.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.modules.identity.dto.ShopManagerRequest;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.identity.service.ShopManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopManagerServiceImpl implements ShopManagerService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BaseResponse updateShop(ShopManagerRequest request) {
        // 1. Lấy thông tin xác thực từ SecurityContext (được nạp bởi JwtFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return BaseResponse.error(401, "Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        // 2. Lấy Email từ Token
        String currentEmail = authentication.getName();

        // 3. Tìm User và Shop liên kết
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng!"));

        Shop shop = user.getShop();
        if (shop == null) {
            return BaseResponse.error(404, "Tài khoản của bạn chưa liên kết với cửa hàng nào!");
        }

        // 4. Kiểm tra các trạng thái logic (Sát với Use Case và Enum của bạn)
        if (shop.getStatus() == ShopStatus.PENDING) {
            return BaseResponse.error(400, "Cửa hàng đang chờ duyệt, không thể thay đổi thông tin!");
        }

        if (shop.getStatus() == ShopStatus.REJECTED) {
            return BaseResponse.error(400, "Cửa hàng đã bị từ chối, vui lòng liên hệ Admin!");
        }

        if (shop.getStatus() == ShopStatus.BANNED) {
            return BaseResponse.error(403, "Cửa hàng đã bị khóa, bạn không có quyền chỉnh sửa!");
        }

        // 5. Cập nhật thông tin từ DTO vào Entity
        shop.setShopName(request.getShopName());
        shop.setPickupAddress(request.getPickupAddress());
        shop.setDescription(request.getDescription());

        // 6. Lưu lại vào CSDL
        shopRepository.save(shop);

        return BaseResponse.successMessage( "Cập nhật thông tin cửa hàng thành công!");
    }
}