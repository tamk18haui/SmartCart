package com.gr6.SmartCart.modules.fulfillment.repository;

import com.gr6.SmartCart.common.domain.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    /**
     * Truy vấn danh sách đơn hàng thuộc về một Shop cụ thể (thông qua email chủ shop)
     * Kết hợp với tìm kiếm theo Mã đơn hàng (ID) hoặc Tên người nhận (ReceiverName)
     * [Luồng cơ bản 1 & Luồng rẽ nhánh 2]
     */
    @Query("SELECT o FROM ShopOrder o WHERE o.shop.user.email = :email " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "OR CAST(o.shopOrderId AS string) LIKE %:keyword% " +
            "OR LOWER(o.order.address.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ShopOrder> searchOrdersByShop(@Param("email") String email, @Param("keyword") String keyword);

    /**
     * Đếm số lượng đơn hàng của shop để phục vụ kiểm tra Luồng rẽ nhánh 1
     */
    long countByShopUserEmail(String email);
}