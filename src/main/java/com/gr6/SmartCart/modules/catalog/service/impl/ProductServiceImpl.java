package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.Category;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.dto.ProductRequest;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;
import com.gr6.SmartCart.modules.catalog.repository.CategoryRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.catalog.service.ProductService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private Shop getCurrentActiveShop() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        Shop shop = user.getShop();

        if (shop == null) {
            throw new RuntimeException("Tài khoản hiện tại chưa đăng ký shop!");
        }

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop chưa được duyệt hoặc đã bị khóa!");
        }

        return shop;
    }

    @Override
    @Transactional
    public BaseResponse<ProductResponse> createProduct(ProductRequest request) {
        Shop shop = getCurrentActiveShop();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        if (category.getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục này đang bị ẩn, không thể đăng sản phẩm!");
        }

        Product product = new Product();
        product.setShop(shop);
        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setCondition(request.getCondition());
        product.setBasePrice(request.getBasePrice());
        product.setWeight(request.getWeight());
        product.setLength(request.getLength());
        product.setWidth(request.getWidth());
        product.setHeight(request.getHeight());
        product.setStatus(ProductStatus.ACTIVE);

        if (request.getUploadImages() != null && !request.getUploadImages().isEmpty()) {
            product.setImageUrls(
                    request.getUploadImages()
                            .stream()
                            .filter(url -> url != null && !url.isBlank())
                            .map(String::trim)
                            .collect(Collectors.joining(","))
            );
        }

        Product savedProduct = productRepository.save(product);

        ProductVariant defaultVariant = new ProductVariant();
        defaultVariant.setProduct(savedProduct);
        defaultVariant.setSku("P-" + savedProduct.getProductId() + "-DEFAULT");
        defaultVariant.setPrice(savedProduct.getBasePrice());
        defaultVariant.setStockQuantity(request.getStockQuantity());
        defaultVariant.setImageUrl(firstImage(savedProduct.getImageUrls()));
        defaultVariant.setIsDefault(true);
        defaultVariant.setStatus(VariantStatus.ACTIVE);

        variantRepository.save(defaultVariant);

        return BaseResponse.success_data(
                "Tạo sản phẩm thành công",
                ProductResponse.fromEntity(savedProduct)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<ProductResponse>> getProductsByShop(Long shopId, int page, int size) {
        Page<Product> products = productRepository.findByShopShopIdAndStatusNot(
                shopId,
                ProductStatus.DELETED,
                PageRequest.of(Math.max(page - 1, 0), size)
        );

        Page<ProductResponse> responsePage = products.map(ProductResponse::fromEntity);

        return BaseResponse.success(PageResponse.of(responsePage));
    }

    @Override
    @Transactional
    public BaseResponse<ProductResponse> updateProduct(Long productId, ProductRequest request) {
        Shop shop = getCurrentActiveShop();

        Product product = productRepository
                .findByProductIdAndShopShopIdAndStatusNot(productId, shop.getShopId(), ProductStatus.DELETED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền sửa!"));

        if (product.getStatus() == ProductStatus.BANNED) {
            throw new RuntimeException("Sản phẩm đang bị admin khóa, seller không thể tự sửa!");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        if (category.getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục này đang bị ẩn!");
        }

        product.setCategory(category);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setCondition(request.getCondition());
        product.setBasePrice(request.getBasePrice());
        product.setWeight(request.getWeight());
        product.setLength(request.getLength());
        product.setWidth(request.getWidth());
        product.setHeight(request.getHeight());

        if (request.getUploadImages() != null) {
            product.setImageUrls(
                    request.getUploadImages()
                            .stream()
                            .filter(url -> url != null && !url.isBlank())
                            .map(String::trim)
                            .collect(Collectors.joining(","))
            );
        }

        Product savedProduct = productRepository.save(product);

        return BaseResponse.success_data(
                "Cập nhật sản phẩm thành công",
                ProductResponse.fromEntity(savedProduct)
        );
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteProduct(Long productId) {
        Shop shop = getCurrentActiveShop();

        Product product = productRepository
                .findByProductIdAndShopShopIdAndStatusNot(productId, shop.getShopId(), ProductStatus.DELETED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền xóa!"));

        if (product.getStatus() == ProductStatus.BANNED) {
            throw new RuntimeException("Sản phẩm đang bị admin khóa, seller không thể tự xóa!");
        }

        product.setStatus(ProductStatus.DELETED);

        if (product.getVariants() != null) {
            product.getVariants().forEach(variant -> variant.setStatus(VariantStatus.DELETED));
        }

        productRepository.save(product);
        

        return BaseResponse.successMessage("Xóa sản phẩm thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<ProductResponse> getProductForSeller(Long productId) {
        Shop shop = getCurrentActiveShop();

        Product product = productRepository
                .findByProductIdAndShopShopIdAndStatusNot(productId, shop.getShopId(), ProductStatus.DELETED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc bạn không có quyền xem!"));

        return BaseResponse.success(ProductResponse.fromEntity(product));
    }

    private String firstImage(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) {
            return null;
        }

        String[] parts = imageUrls.split(",");
        return parts.length == 0 ? null : parts[0].trim();
    }
}