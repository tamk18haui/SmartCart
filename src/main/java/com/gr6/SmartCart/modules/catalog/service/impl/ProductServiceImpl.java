package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.Category;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.common.enums.UserStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.dto.ProductRequest;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;
import com.gr6.SmartCart.modules.catalog.repository.CatalogOrderItemRepository;
import com.gr6.SmartCart.modules.catalog.repository.CatalogReviewRepository;
import com.gr6.SmartCart.modules.catalog.repository.CategoryRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.catalog.service.ProductService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CatalogReviewRepository catalogReviewRepository;
    private final CatalogOrderItemRepository catalogOrderItemRepository;
    private final ProductVariantRepository variantRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Lỗi xác thực: Không tìm thấy người dùng!"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa!");
        }
        if (user.getRole() != UserRole.SELLER) {
            throw new RuntimeException("Chỉ seller mới được quản lý sản phẩm!");
        }
        return user;
    }

    private Shop getCurrentShop() {
        User user = getCurrentUser();
        if (user.getShop() == null) {
            throw new RuntimeException("Bạn chưa đăng ký mở Shop!");
        }
        if (user.getShop().getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop của bạn chưa được duyệt hoặc đã bị khóa!");
        }
        return user.getShop();
    }

    private Category getActiveCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        if (category.getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục đang bị khóa, không thể tạo/cập nhật sản phẩm!");
        }
        return category;
    }

    private void validateSellerOwnsProduct(Product product, Shop currentShop) {
        if (product.getShop() == null || !product.getShop().getShopId().equals(currentShop.getShopId())) {
            throw new RuntimeException("Bạn không có quyền thao tác sản phẩm này!");
        }
    }

    @Override
    @Transactional
    public BaseResponse<ProductResponse> createProduct(ProductRequest request) {
        Category category = getActiveCategory(request.getCategoryId());
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

        if (request.getUploadImages() != null && !request.getUploadImages().isEmpty()) {
            product.setImageUrls(String.join(",", request.getUploadImages()));
        }

        Product savedProduct = productRepository.save(product);

        ProductVariant defaultVariant = new ProductVariant();
        defaultVariant.setProduct(savedProduct);
        defaultVariant.setSku("SKU-" + savedProduct.getProductId() + "-DEFAULT");
        defaultVariant.setPrice(request.getBasePrice());
        defaultVariant.setStockQuantity(request.getStockQuantity());
        defaultVariant.setStatus(VariantStatus.ACTIVE);
        if (request.getUploadImages() != null && !request.getUploadImages().isEmpty()) {
            defaultVariant.setImageUrl(request.getUploadImages().get(0));
        }
        variantRepository.save(defaultVariant);

        savedProduct.setVariants(new ArrayList<>());
        savedProduct.getVariants().add(defaultVariant);

        return BaseResponse.success_data("Tạo sản phẩm thành công", enrichResponse(savedProduct));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<ProductResponse>> getProductsByShop(Long shopId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Product> productPage = productRepository.findByShopShopIdAndStatusNot(shopId, ProductStatus.DELETED, pageable);

        PageResponse<ProductResponse> response = PageResponse.<ProductResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .data(productPage.getContent().stream().map(this::enrichResponse).collect(Collectors.toList()))
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .build();

        return BaseResponse.success_data("Lấy danh sách sản phẩm thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<ProductResponse> getProductForSeller(Long productId) {
        Shop currentShop = getCurrentShop();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        validateSellerOwnsProduct(product, currentShop);
        return BaseResponse.success_data("Lấy sản phẩm thành công", enrichResponse(product));
    }

    @Override
    @Transactional
    public BaseResponse<ProductResponse> updateProduct(Long productId, ProductRequest request) {
        Shop currentShop = getCurrentShop();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        validateSellerOwnsProduct(product, currentShop);

        if (product.getStatus() == ProductStatus.BANNED || product.getStatus() == ProductStatus.DELETED) {
            throw new RuntimeException("Sản phẩm đã bị khóa/xóa, không thể cập nhật!");
        }

        Category category = getActiveCategory(request.getCategoryId());

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

        if (request.getUploadImages() != null) {
            product.setImageUrls(String.join(",", request.getUploadImages()));
        }

        Product savedProduct = productRepository.save(product);
        return BaseResponse.success_data("Cập nhật sản phẩm thành công", enrichResponse(savedProduct));
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteProduct(Long productId) {
        Shop currentShop = getCurrentShop();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        validateSellerOwnsProduct(product, currentShop);

        if (product.getStatus() == ProductStatus.BANNED) {
            throw new RuntimeException("Sản phẩm đang bị admin khóa, seller không được xóa!");
        }

        product.setStatus(ProductStatus.DELETED);
        if (product.getVariants() != null) {
            product.getVariants().forEach(v -> v.setStatus(VariantStatus.DELETED));
        }
        productRepository.save(product);

        return BaseResponse.successMessage("Xóa sản phẩm thành công!");
    }

    private ProductResponse enrichResponse(Product product) {
        ProductResponse response = ProductResponse.fromEntity(product);
        response.setAverageRating(catalogReviewRepository.getAverageRatingByProductId(product.getProductId()));
        response.setSoldQuantity(catalogOrderItemRepository.getSoldQuantityByProductId(product.getProductId(), OrderStatus.DELIVERED));
        return response;
    }
}