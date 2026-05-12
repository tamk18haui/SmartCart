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
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.identity.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAccountServiceImpl implements AdminAccountService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    private String getCurrentAdminEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public BaseResponse<PageResponse<UserAdminResponse>> getUsers(int page, int size, UserRole role, UserStatus status, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> users = userRepository.searchForAdmin(role, status, keyword, pageable);
        return BaseResponse.success_data("Thành công", PageResponse.of(users.map(UserAdminResponse::fromEntity)));
    }

    @Override
    @Transactional
    public BaseResponse<String> banUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy User!"));
        String currentAdmin = getCurrentAdminEmail();

        if (user.getRole() == UserRole.ADMIN) throw new RuntimeException("Không được khóa tài khoản ADMIN");
        if (user.getEmail().equals(currentAdmin)) throw new RuntimeException("Không được tự khóa chính mình");

        user.setStatus(UserStatus.BANNED);

        if (user.getRole() == UserRole.SELLER && user.getShop() != null) {
            Shop shop = user.getShop();
            shop.setStatus(ShopStatus.BANNED);

            if (shop.getProducts() != null) {
                for (Product p : shop.getProducts()) {
                    if (p.getStatus() == ProductStatus.ACTIVE || p.getStatus() == ProductStatus.HIDDEN) {
                        p.setPreviousStatus(p.getStatus());
                        p.setStatus(ProductStatus.BANNED);
                        p.setBanReason("Chủ shop bị admin khóa");
                        p.setBannedAt(LocalDateTime.now());
                        p.setBannedBy(currentAdmin);
                    }
                }
            }
        }

        userRepository.save(user);
        return BaseResponse.successMessage("Đã khóa User: " + user.getEmail());
    }

    @Override
    @Transactional
    public BaseResponse<String> unbanUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Không tìm thấy User!"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return BaseResponse.successMessage("Đã mở khóa User: " + user.getEmail());
    }

    @Override
    public BaseResponse<PageResponse<ShopAdminResponse>> getShops(int page, int size, ShopStatus status, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Shop> shops = shopRepository.searchForAdmin(status, keyword, pageable);
        return BaseResponse.success_data("Thành công", PageResponse.of(shops.map(ShopAdminResponse::fromEntity)));
    }

    @Override
    @Transactional
    public BaseResponse<String> approveShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Không tìm thấy Shop!"));
        if (shop.getStatus() != ShopStatus.PENDING) throw new RuntimeException("Shop không ở trạng thái chờ duyệt!");

        shop.setStatus(ShopStatus.ACTIVE);
        shop.getUser().setRole(UserRole.SELLER);
        shopRepository.save(shop);
        return BaseResponse.successMessage("Đã duyệt Shop: " + shop.getShopName());
    }

    @Override
    @Transactional
    public BaseResponse<String> rejectShop(Long shopId, String reason) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Không tìm thấy Shop!"));
        shop.setStatus(ShopStatus.REJECTED);
        shopRepository.save(shop);
        return BaseResponse.successMessage("Đã từ chối Shop: " + shop.getShopName());
    }

    @Override
    @Transactional
    public BaseResponse<String> banShop(Long shopId, String reason) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Không tìm thấy Shop!"));
        String currentAdmin = getCurrentAdminEmail();

        shop.setStatus(ShopStatus.BANNED);

        if (shop.getProducts() != null) {
            for (Product p : shop.getProducts()) {
                if (p.getStatus() == ProductStatus.ACTIVE || p.getStatus() == ProductStatus.HIDDEN) {
                    p.setPreviousStatus(p.getStatus());
                    p.setStatus(ProductStatus.BANNED);
                    p.setBanReason("Shop bị admin khóa: " + reason);
                    p.setBannedAt(LocalDateTime.now());
                    p.setBannedBy(currentAdmin);
                }
            }
        }
        shopRepository.save(shop);
        return BaseResponse.successMessage("Đã khóa Shop: " + shop.getShopName());
    }

    @Override
    @Transactional
    public BaseResponse<String> unbanShop(Long shopId) {
        Shop shop = shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Không tìm thấy Shop!"));
        shop.setStatus(ShopStatus.ACTIVE);
        shopRepository.save(shop);
        return BaseResponse.successMessage("Đã mở khóa Shop: " + shop.getShopName());
    }
}