// Đường dẫn: src/main/java/com/gr6/SmartCart/modules/finance_core/service/VoucherService.java
package com.gr6.SmartCart.modules.finance_core.service;

public interface VoucherService {
    // Yêu cầu truyền thêm userId
    Long calculateDiscount(String code, Long totalAmount, Long shopId, Long userId);
    void useVoucher(String code, Long userId);
}