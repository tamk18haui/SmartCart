package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.modules.catalog.dto.ProductRequest;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;

public interface ProductService {
    BaseResponse<ProductResponse> createProduct(ProductRequest request);
    BaseResponse<PageResponse<ProductResponse>> getProductsByShop(Long shopId, int page, int size);
    BaseResponse<String> deleteProduct(Long productId);
    BaseResponse<ProductResponse> updateProduct(Long productId, ProductRequest request);

    // API cho Chủ Shop (Xem cả hàng ẩn, xóa)
    BaseResponse<ProductResponse> getProductForSeller(Long productId);
}