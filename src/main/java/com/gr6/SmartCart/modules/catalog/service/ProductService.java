package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.modules.catalog.dto.ProductRequest;
import java.util.List;

public interface ProductService {
    BaseResponse<Product> createProduct(ProductRequest request);
    BaseResponse<List<Product>> getProductsByShop(Long shopId);
    BaseResponse<String> deleteProduct(Long productId);
}