package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.modules.catalog.dto.ProductResponse;

public interface AdminProductService {
    BaseResponse<PageResponse<ProductResponse>> getProducts(int page, int size, ProductStatus status, String keyword);
    BaseResponse<String> banProduct(Long productId, String reason);
    BaseResponse<String> unbanProduct(Long productId);
}