package com.gr6.SmartCart.modules.identity.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.modules.identity.dto.ShopInfoResponse;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return BaseResponse.error(401, "Bạn cần đăng nhập để thực hiện thao tác này!");
        }

        String currentEmail = authentication.getName();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng!"));

        Shop shop = shopRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin cửa hàng!"));

        if (shop.getStatus() == ShopStatus.PENDING) {
            return BaseResponse.error(400, "Cửa hàng đang chờ duyệt, không thể thay đổi thông tin!");
        }

        if (shop.getStatus() == ShopStatus.REJECTED) {
            return BaseResponse.error(400, "Cửa hàng đã bị từ chối, vui lòng liên hệ Admin!");
        }

        if (shop.getStatus() == ShopStatus.BANNED) {
            return BaseResponse.error(403, "Cửa hàng đã bị khóa, bạn không có quyền chỉnh sửa!");
        }

        shop.setShopName(request.getShopName().trim());
        shop.setPickupAddress(request.getPickupAddress().trim());
        shop.setDescription(trimToNull(request.getDescription()));

        if (request.getLogoUrl() != null) {
            shop.setLogoUrl(trimToNull(request.getLogoUrl()));
        }

        if (request.getCoverUrl() != null) {
            shop.setCoverUrl(trimToNull(request.getCoverUrl()));
        }

        shopRepository.save(shop);

        return BaseResponse.successMessage("Cập nhật thông tin cửa hàng thành công!");
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse getShopInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return BaseResponse.error(401, "Bạn cần đăng nhập để xem thông tin cửa hàng!");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Shop shop = shopRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return BaseResponse.success(mapToResponse(shop));
    }

    private ShopInfoResponse mapToResponse(Shop shop) {
        return ShopInfoResponse.builder()
                .shopId(shop.getShopId())
                .shopName(shop.getShopName())
                .pickupAddress(shop.getPickupAddress())
                .description(shop.getDescription())
                .status(shop.getStatus() == null ? null : shop.getStatus().name())
                .logoUrl(shop.getLogoUrl())
                .coverUrl(shop.getCoverUrl())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
