package com.gr6.SmartCart.modules.catalog.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.modules.catalog.dto.VariantCreateRequest;

public interface ProductVariantService {
    BaseResponse<ProductVariant> createVariant(VariantCreateRequest request);
}