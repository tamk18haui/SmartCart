package com.gr6.SmartCart.modules.identity.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.common.enums.UserStatus;
import com.gr6.SmartCart.modules.identity.dto.ShopAdminResponse;
import com.gr6.SmartCart.modules.identity.dto.UserAdminResponse;
import com.gr6.SmartCart.modules.identity.service.AdminAccountService;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAccountServiceImpl implements AdminAccountService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<UserAdminResponse>> getUsers(int page, int size, UserRole role, UserStatus status, String keyword) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<UserAdminResponse> result = userRepository
                .searchForAdmin(role, status, normalizeKeyword(keyword), pageable)
                .map(UserAdminResponse::fromEntity);

        return BaseResponse.success_data("Thành công", PageResponse.of(result));
    }

    @Override
    @Transactional
    public BaseResponse<String> banUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return BaseResponse.error(404, "Không tìm thấy tài khoản");
        }
        if (user.getRole() == UserRole.ADMIN) {
            return BaseResponse.error(403, "Không được khóa tài khoản ADMIN");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            return BaseResponse.error(400, "Tài khoản đã bị khóa trước đó");
        }

        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);

        if (user.getRole() == UserRole.SELLER) {
            banProductsOfSeller(user.getUserId(), "Vi phạm chính sách hệ thống");
        }

        return BaseResponse.successMessage("Đã khóa tài khoản thành công");
    }

    @Override
    @Transactional
    public BaseResponse<String> unbanUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return BaseResponse.error(404, "Không tìm thấy tài khoản");
        }
        if (user.getStatus() != UserStatus.BANNED) {
            return BaseResponse.error(400, "Tài khoản chưa bị khóa");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        if (user.getRole() == UserRole.SELLER) {
            restoreProductsByMarker("USER_BAN:" + user.getUserId());
        }

        return BaseResponse.successMessage("Đã mở khóa tài khoản thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<ShopAdminResponse>> getShops(int page, int size, ShopStatus status, String keyword) {
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));
        Page<ShopAdminResponse> result = shopRepository
                .searchForAdmin(status, normalizeKeyword(keyword), pageable)
                .map(ShopAdminResponse::fromEntity);

        return BaseResponse.success_data("Thành công", PageResponse.of(result));
    }

    @Override
    @Transactional
    public BaseResponse<String> approveShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            return BaseResponse.error(404, "Không tìm thấy shop");
        }
        if (shop.getStatus() == ShopStatus.ACTIVE) {
            return BaseResponse.error(400, "Shop đã được duyệt trước đó");
        }
        if (shop.getStatus() == ShopStatus.BANNED) {
            return BaseResponse.error(400, "Shop đang bị khóa, cần mở khóa thay vì duyệt");
        }

        shop.setStatus(ShopStatus.ACTIVE);
        shopRepository.save(shop);

        return BaseResponse.successMessage("Đã duyệt shop thành công");
    }

    @Override
    @Transactional
    public BaseResponse<String> rejectShop(Long shopId, String reason) {
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            return BaseResponse.error(404, "Không tìm thấy shop");
        }
        if (shop.getStatus() == ShopStatus.BANNED) {
            return BaseResponse.error(400, "Shop đang bị khóa, không thể từ chối");
        }

        shop.setStatus(ShopStatus.REJECTED);
        shopRepository.save(shop);

        return BaseResponse.successMessage("Đã từ chối shop thành công");
    }

    @Override
    @Transactional
    public BaseResponse<String> banShop(Long shopId, String reason) {
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            return BaseResponse.error(404, "Không tìm thấy shop");
        }
        if (shop.getStatus() == ShopStatus.BANNED) {
            return BaseResponse.error(400, "Shop đã bị khóa trước đó");
        }

        shop.setStatus(ShopStatus.BANNED);
        banProductsOfShop(shop, reason);
        shopRepository.save(shop);

        return BaseResponse.successMessage("Đã khóa shop thành công");
    }

    @Override
    @Transactional
    public BaseResponse<String> unbanShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            return BaseResponse.error(404, "Không tìm thấy shop");
        }
        if (shop.getStatus() != ShopStatus.BANNED) {
            return BaseResponse.error(400, "Shop chưa bị khóa");
        }

        shop.setStatus(ShopStatus.ACTIVE);
        restoreProductsByMarker("SHOP_BAN:" + shop.getShopId());
        shopRepository.save(shop);

        return BaseResponse.successMessage("Đã mở khóa shop thành công");
    }

    private void banProductsOfSeller(Long userId, String reason) {
        List<Shop> shops = shopRepository.findAllByUser_UserId(userId);
        for (Shop shop : shops) {
            if (shop.getProducts() == null) {
                continue;
            }
            String marker = "USER_BAN:" + userId;
            for (Product product : shop.getProducts()) {
                banProductByCascade(product, marker, "Tài khoản seller bị khóa", reason);
            }
        }
    }

    private void banProductsOfShop(Shop shop, String reason) {
        if (shop.getProducts() == null) {
            return;
        }
        String marker = "SHOP_BAN:" + shop.getShopId();
        for (Product product : shop.getProducts()) {
            banProductByCascade(product, marker, "Shop bị khóa", reason);
        }
    }

    private void banProductByCascade(Product product, String marker, String defaultReason, String reason) {
        if (product == null || product.getStatus() == ProductStatus.DELETED || product.getStatus() == ProductStatus.BANNED) {
            return;
        }
        product.setPreviousStatus(product.getStatus() == null ? ProductStatus.HIDDEN : product.getStatus());
        product.setStatus(ProductStatus.BANNED);
        product.setBanReason(isBlank(reason) ? defaultReason : reason.trim());
        product.setBannedAt(LocalDateTime.now());
        product.setBannedBy(marker);
    }

    private void restoreProductsByMarker(String marker) {
        if (isBlank(marker)) {
            return;
        }

        List<Shop> allShops = shopRepository.findAll();
        for (Shop shop : allShops) {
            if (shop.getProducts() == null) {
                continue;
            }
            for (Product product : shop.getProducts()) {
                if (product.getStatus() != ProductStatus.BANNED) {
                    continue;
                }
                if (!marker.equals(product.getBannedBy())) {
                    continue;
                }

                ProductStatus restoreStatus = product.getPreviousStatus();
                if (restoreStatus == null || restoreStatus == ProductStatus.BANNED || restoreStatus == ProductStatus.DELETED) {
                    restoreStatus = ProductStatus.HIDDEN;
                }
                product.setStatus(restoreStatus);
                product.setPreviousStatus(null);
                product.setBanReason(null);
                product.setBannedAt(null);
                product.setBannedBy(null);
            }
        }
    }

    private int normalizePage(int page) {
        return Math.max(page - 1, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) return 10;
        return Math.min(size, 100);
    }

    private String normalizeKeyword(String keyword) {
        return isBlank(keyword) ? null : keyword.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String currentAdminEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "SYSTEM_ADMIN";
        }
        return authentication.getName();
    }
}