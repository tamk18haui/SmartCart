package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.modules.catalog.dto.*;
import com.gr6.SmartCart.modules.catalog.repository.*;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // Các Repository phụ trợ của Catalog
    private final CatalogReviewRepository catalogReviewRepository;
    private final CatalogOrderItemRepository catalogOrderItemRepository;

    // THÊM MỚI: Bơm kho Biến thể vào để tự tạo biến thể mặc định
    private final ProductVariantRepository variantRepository;

    private Shop getCurrentShop() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực: Không tìm thấy người dùng!"));
        if (user.getShop() == null) {
            throw new RuntimeException("Bạn chưa đăng ký mở Shop!");
        }
        return user.getShop();
    }

    @Override
    @Transactional
    public BaseResponse<ProductResponse> createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        Shop currentShop = getCurrentShop();

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setCondition(request.getCondition());
        product.setBasePrice(request.getBasePrice());
        product.setWeight(request.getWeight());
        product.setLength(request.getLength());
        product.setWidth(request.getWidth());
        product.setHeight(request.getHeight());
        product.setStatus(ProductStatus.ACTIVE);
        product.setCategory(category);
        product.setShop(currentShop);

        // 1. Lưu sản phẩm gốc trước
        Product savedProduct = productRepository.save(product);

        // 2. TỰ ĐỘNG TẠO BIẾN THỂ MẶC ĐỊNH
        ProductVariant defaultVariant = new ProductVariant();
        defaultVariant.setProduct(savedProduct);
        defaultVariant.setSku("SP" + savedProduct.getProductId() + "-DEFAULT"); // Tạo mã SKU ngẫu nhiên nhưng duy nhất
        defaultVariant.setPrice(savedProduct.getBasePrice());
        defaultVariant.setStockQuantity(request.getStockQuantity());

        ProductVariant savedVariant = variantRepository.save(defaultVariant);

        // Gắn biến thể vừa tạo vào Response để trả về cho Frontend hiển thị ngay
        savedProduct.setVariants(new ArrayList<>());
        savedProduct.getVariants().add(savedVariant);

        return BaseResponse.success_data("Đã đăng sản phẩm thành công!", ProductResponse.fromEntity(savedProduct));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<ProductResponse>> getProductsByShop(Long shopId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> productPage = productRepository.findByShopShopIdAndStatusNot(
                shopId, ProductStatus.DELETED, pageable
        );

        Page<ProductResponse> responsePage = productPage.map(product -> {
            ProductResponse res = ProductResponse.fromEntity(product);

            Double avgRating = catalogReviewRepository.getAverageRatingByProductId(product.getProductId());
            res.setAverageRating(Math.round(avgRating * 10.0) / 10.0);

            res.setSoldQuantity(catalogOrderItemRepository.getSoldQuantityByProductId(product.getProductId(), OrderStatus.DELIVERED));
            return res;
        });

        return BaseResponse.success_data("Lấy danh sách sản phẩm thành công", PageResponse.of(responsePage));
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để xóa"));

        Shop currentShop = getCurrentShop();
        if (!product.getShop().getShopId().equals(currentShop.getShopId())) {
            return BaseResponse.error(403, "Cảnh báo: Bạn không có quyền xóa sản phẩm của người khác!");
        }

        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);

        return BaseResponse.successMessage("Đã xóa sản phẩm thành công!");
    }

    @Override
    @Transactional
    public BaseResponse<ProductResponse> updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        Shop currentShop = getCurrentShop();
        if (!product.getShop().getShopId().equals(currentShop.getShopId())) {
            return BaseResponse.error(403, "Cảnh báo: Bạn không có quyền sửa sản phẩm của người khác!");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục mới!"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setCondition(request.getCondition());
        product.setBasePrice(request.getBasePrice());
        product.setWeight(request.getWeight());
        product.setLength(request.getLength());
        product.setWidth(request.getWidth());
        product.setHeight(request.getHeight());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);
        return BaseResponse.success_data("Cập nhật sản phẩm thành công!", ProductResponse.fromEntity(updatedProduct));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<ProductResponse> getProductForSeller(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        Shop currentShop = getCurrentShop();
        if (!product.getShop().getShopId().equals(currentShop.getShopId())) {
            return BaseResponse.error(403, "Bạn không có quyền truy cập sản phẩm này!");
        }

        if (product.getStatus() == ProductStatus.DELETED) {
            return BaseResponse.error(404, "Sản phẩm này đã bị xóa!");
        }

        ProductResponse res = ProductResponse.fromEntity(product);

        Double avgRating = catalogReviewRepository.getAverageRatingByProductId(productId);
        res.setAverageRating(Math.round(avgRating * 10.0) / 10.0);

        res.setSoldQuantity(catalogOrderItemRepository.getSoldQuantityByProductId(productId, OrderStatus.DELIVERED));

        return BaseResponse.success_data("Lấy chi tiết thành công", res);
    }
}