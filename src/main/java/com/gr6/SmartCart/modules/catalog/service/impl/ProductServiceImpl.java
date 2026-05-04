package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Category;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.modules.catalog.dto.ProductRequest;
import com.gr6.SmartCart.modules.catalog.repository.CategoryRepository;
import com.gr6.SmartCart.modules.catalog.repository.ProductRepository;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository; // Giả định ShopRepo nằm ở đây[cite: 1]
import com.gr6.SmartCart.modules.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public BaseResponse<Product> createProduct(ProductRequest request) {
        // 1. Tìm Category và Shop xem có tồn tại không
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        Shop shop = shopRepository.findById(Math.toIntExact(request.getShopId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Shop!"));

        // 2. Chuyển DTO sang Entity Product
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
        product.setShop(shop);

        Product savedProduct = productRepository.save(product);
        return BaseResponse.success_data("Đã đăng sản phẩm thành công!", savedProduct);
    }

    @Override
    public BaseResponse<List<Product>> getProductsByShop(Long shopId) {
        List<Product> products = productRepository.findByShopShopId(shopId);
        return BaseResponse.success_data("Lấy danh sách sản phẩm thành công", products);
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            return BaseResponse.error(404, "Không tìm thấy sản phẩm để xóa");
        }
        productRepository.deleteById(productId);
        return BaseResponse.successMessage("Đã xóa sản phẩm thành công");
    }
}