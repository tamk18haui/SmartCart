package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.catalog.dto.VariantCreateRequest;
import com.gr6.SmartCart.modules.catalog.dto.VariantResponse;

public interface ProductVariantService {
    BaseResponse<VariantResponse> createVariant(VariantCreateRequest request);
    BaseResponse<VariantResponse> updateVariant(Long variantId, VariantCreateRequest request);
    BaseResponse<String> deleteVariant(Long variantId);
}