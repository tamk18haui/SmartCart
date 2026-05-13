package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;

public interface AdminProductService {

    BaseResponse<PageResponse<ProductResponse>> getProducts(
            String keyword,
            ProductStatus status,
            Long shopId,
            Long categoryId,
            int page,
            int size
    );

    BaseResponse<String> banProduct(Long productId, String reason);

    BaseResponse<String> unbanProduct(Long productId);

    BaseResponse<String> deleteProduct(Long productId);
}