package com.gr6.SmartCart.modules.storefront.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.modules.storefront.dto.ShopProductResponse;
import com.gr6.SmartCart.modules.storefront.dto.ShopPublicResponse;

public interface ShopPublicService {

    BaseResponse<ShopPublicResponse> getShopDetail(Long shopId);

    BaseResponse<PageResponse<ShopProductResponse>> getShopProducts(
            Long shopId,
            int page,
            int size
    );
}