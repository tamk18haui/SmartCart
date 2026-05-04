package com.gr6.SmartCart.modules.identity.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.identity.dto.LoginRequest;

public interface LoginService {
    /**
     * Xác thực email và mật khẩu người dùng
     */
    BaseResponse<Object> login(LoginRequest request);
}