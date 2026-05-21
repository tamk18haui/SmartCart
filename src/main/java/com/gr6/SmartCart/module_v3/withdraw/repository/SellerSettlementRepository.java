package com.gr6.SmartCart.module_v3.withdraw.repository;

import com.gr6.SmartCart.common.domain.SellerSettlement;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerSettlementRepository extends JpaRepository<SellerSettlement, Long> {

    boolean existsByShopOrder_ShopOrderId(Long shopOrderId);

    Page<SellerSettlement> findBySeller_EmailOrderBySettlementIdDesc(
            String sellerEmail,
            Pageable pageable
    );

    Page<SellerSettlement> findAllByOrderBySettlementIdDesc(Pageable pageable);
}