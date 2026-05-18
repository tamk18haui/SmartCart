package com.gr6.SmartCart.modules.identity.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.identity.dto.ShopRegisterRequest;

public interface ShopRegistrationService {
    // Chỉ truyền vào Request, vì mình tạo User mới ngay trong này luôn
    BaseResponse<String> registerShop(ShopRegisterRequest request);
}