package com.gr6.SmartCart.modules.identity.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.identity.dto.RegisterRequest;

public interface RegisterService {
    /**
     * Xử lý đăng ký tài khoản người dùng mới (Role: BUYER)
     */
    BaseResponse<String> register(RegisterRequest request);
}