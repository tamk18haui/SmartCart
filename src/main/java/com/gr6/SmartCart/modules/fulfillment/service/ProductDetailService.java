package com.gr6.SmartCart.modules.fulfillment.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.ProductDetailResponse;

public interface ProductDetailService {
    BaseResponse<ProductDetailResponse> getProductDetail(Long id);
}