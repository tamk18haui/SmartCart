package com.gr6.SmartCart.modules.catalog.repository;

import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /*
     * Dùng cho ProductVariantServiceImpl khi thêm biến thể:
     * kiểm tra SKU đã tồn tại trong cùng 1 sản phẩm chưa.
     */
    boolean existsByProductProductIdAndSku(Long productId, String sku);

    /*
     * Dùng cho ProductVariantServiceImpl khi sửa biến thể:
     * kiểm tra SKU trùng với biến thể khác trong cùng sản phẩm.
     */
    boolean existsByProductProductIdAndSkuAndVariantIdNot(Long productId, String sku, Long variantId);

    List<ProductVariant> findByProductProductId(Long productId);

    List<ProductVariant> findByProductProductIdAndStatusNot(Long productId, VariantStatus status);

    Optional<ProductVariant> findByVariantIdAndProductShopShopIdAndStatusNot(
            Long variantId,
            Long shopId,
            VariantStatus status
    );

    /*
     * Dùng cho quản lý kho seller:
     * lấy toàn bộ biến thể thuộc shop hiện tại, có thể lọc theo tên sản phẩm hoặc SKU.
     */
    @Query("""
            SELECT v FROM ProductVariant v
            JOIN FETCH v.product p
            JOIN FETCH p.shop s
            LEFT JOIN FETCH p.category c
            WHERE s.shopId = :shopId
              AND v.status <> :variantDeletedStatus
              AND p.status <> :productDeletedStatus
              AND (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY v.stockQuantity ASC, p.productId DESC, v.variantId DESC
            """)
    List<ProductVariant> findSellerInventory(
            @Param("shopId") Long shopId,
            @Param("keyword") String keyword,
            @Param("variantDeletedStatus") VariantStatus variantDeletedStatus,
            @Param("productDeletedStatus") ProductStatus productDeletedStatus
    );

    /*
     * Dùng khi đặt hàng / hủy đơn / hoàn kho để tránh race condition tồn kho.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.variantId = :variantId")
    Optional<ProductVariant> findByIdWithLock(@Param("variantId") Long variantId);
}