package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.common.enums.UserStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.dto.InventoryUpdateRequest;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.catalog.service.InventoryService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản đang đăng nhập!"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa, không thể thao tác tồn kho!");
        }
        return user;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0!");
        }
    }

    private void validateSellerCanManageVariant(ProductVariant variant) {
        User user = getCurrentUser();

        if (variant.getStatus() == VariantStatus.DELETED || variant.getStatus() == VariantStatus.HIDDEN) {
            throw new RuntimeException("Biến thể đang bị ẩn/xóa, không thể chỉnh tồn kho!");
        }

        Product product = variant.getProduct();
        if (product == null) {
            throw new RuntimeException("Biến thể không gắn với sản phẩm hợp lệ!");
        }

        if (product.getStatus() == ProductStatus.BANNED || product.getStatus() == ProductStatus.DELETED) {
            throw new RuntimeException("Sản phẩm đã bị khóa/xóa, không thể chỉnh tồn kho!");
        }

        if (product.getCategory() == null || product.getCategory().getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục của sản phẩm đang bị khóa, không thể chỉnh tồn kho!");
        }

        Shop shop = product.getShop();
        if (shop == null || shop.getUser() == null) {
            throw new RuntimeException("Sản phẩm chưa gắn với shop hợp lệ!");
        }

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop chưa hoạt động hoặc đã bị khóa, không thể chỉnh tồn kho!");
        }

        if (user.getRole() != UserRole.SELLER) {
            throw new RuntimeException("Chỉ seller mới được chỉnh tồn kho sản phẩm!");
        }

        if (user.getShop() == null || !shop.getShopId().equals(user.getShop().getShopId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh tồn kho biến thể này!");
        }
    }

    @Override
    @Transactional
    public BaseResponse<String> decreaseStock(InventoryUpdateRequest request) {
        validateQuantity(request.getQuantity());

        ProductVariant variant = productVariantRepository.findByIdWithLock(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

        validateSellerCanManageVariant(variant);

        if (variant.getStockQuantity() == null || variant.getStockQuantity() < request.getQuantity()) {
            return BaseResponse.error(400, "Số lượng tồn kho không đủ!");
        }

        variant.setStockQuantity(variant.getStockQuantity() - request.getQuantity());
        productVariantRepository.save(variant);

        return BaseResponse.successMessage("Đã trừ kho thành công!");
    }

    @Override
    @Transactional
    public BaseResponse<String> increaseStock(InventoryUpdateRequest request) {
        validateQuantity(request.getQuantity());

        ProductVariant variant = productVariantRepository.findByIdWithLock(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

        validateSellerCanManageVariant(variant);

        int currentStock = variant.getStockQuantity() == null ? 0 : variant.getStockQuantity();
        variant.setStockQuantity(currentStock + request.getQuantity());
        productVariantRepository.save(variant);

        return BaseResponse.successMessage("Đã cộng thêm " + request.getQuantity() + " sản phẩm vào kho!");
    }
}