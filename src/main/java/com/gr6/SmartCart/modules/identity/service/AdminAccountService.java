package com.gr6.SmartCart.modules.identity.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.common.enums.UserStatus;
import com.gr6.SmartCart.modules.identity.dto.ShopAdminResponse;
import com.gr6.SmartCart.modules.identity.dto.UserAdminResponse;

public interface AdminAccountService {
    // Quản lý Buyer & Seller (Users)
    BaseResponse<PageResponse<UserAdminResponse>> getUsers(int page, int size, UserRole role, UserStatus status, String keyword);
    BaseResponse<String> banUser(Long userId);
    BaseResponse<String> unbanUser(Long userId);

    // Quản lý Shop (Cửa hàng của Seller)
    BaseResponse<PageResponse<ShopAdminResponse>> getShops(int page, int size, ShopStatus status, String keyword);
    BaseResponse<String> approveShop(Long shopId);
    BaseResponse<String> rejectShop(Long shopId, String reason);
    BaseResponse<String> banShop(Long shopId, String reason);
    BaseResponse<String> unbanShop(Long shopId);
}