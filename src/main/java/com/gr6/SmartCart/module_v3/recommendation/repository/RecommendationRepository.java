package com.gr6.SmartCart.module_v3.recommendation.repository;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Product, Long> {

    /**
     * Lấy tất cả sản phẩm ACTIVE có shop ACTIVE và danh mục ACTIVE
     */
    @Query("SELECT DISTINCT p FROM Product p " +
           "JOIN FETCH p.shop s " +
           "JOIN FETCH p.category c " +
           "WHERE p.status = :productStatus " +
           "AND s.status = 'ACTIVE' " +
           "AND c.categoryStatus = 'ACTIVE'")
    List<Product> findAllActiveProducts(@Param("productStatus") ProductStatus productStatus);

    /**
     * Lấy khoảng giá (min, max) của các variant ACTIVE theo từng sản phẩm
     */
    @Query("SELECT v.product.productId, MIN(v.price), MAX(v.price) " +
           "FROM ProductVariant v " +
           "WHERE v.product.status = :productStatus " +
           "AND v.status = 'ACTIVE' " +
           "GROUP BY v.product.productId")
    List<Object[]> findVariantPriceRanges(@Param("productStatus") ProductStatus productStatus);

    /**
     * Đếm số lượng mua theo sản phẩm trong khoảng thời gian (đơn DELIVERED/COMPLETED)
     */
    @Query("SELECT oi.variant.product.productId, COALESCE(SUM(oi.quantity), 0) " +
           "FROM OrderItem oi " +
           "WHERE oi.shopOrder.order.status IN :statuses " +
           "AND oi.shopOrder.order.createdAt >= :since " +
           "AND oi.variant.product.status = :productStatus " +
           "GROUP BY oi.variant.product.productId")
    List<Object[]> findPurchaseCountsSince(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("since") LocalDateTime since,
            @Param("productStatus") ProductStatus productStatus);

    /**
     * Đếm số lượng sản phẩm đang nằm trong giỏ hàng (tất cả user)
     */
    @Query("SELECT ci.variant.product.productId, COALESCE(SUM(ci.quantity), 0) " +
           "FROM CartItem ci " +
           "WHERE ci.variant.product.status = :productStatus " +
           "GROUP BY ci.variant.product.productId")
    List<Object[]> findCartCountsByProduct(@Param("productStatus") ProductStatus productStatus);

    /**
     * Lấy đánh giá trung bình và số lượng review theo sản phẩm
     */
    @Query("SELECT r.product.productId, AVG(r.rating), COUNT(r) " +
           "FROM Review r " +
           "GROUP BY r.product.productId")
    List<Object[]> findRatingsByProduct();

    /**
     * Tìm sản phẩm hay được mua kèm (co-purchase) từ hóa đơn của người khác.
     * Ý tưởng: Tìm tất cả đơn hàng chứa sản phẩm X, rồi lấy các sản phẩm KHÁC trong cùng đơn đó.
     * COUNT(DISTINCT o) = số đơn hàng mà 2 sản phẩm cùng xuất hiện.
     */
    @Query("SELECT oi2.variant.product.productId, COUNT(DISTINCT o) " +
           "FROM OrderItem oi1 " +
           "JOIN oi1.shopOrder so1 " +
           "JOIN so1.order o " +
           "JOIN o.shopOrders so2 " +
           "JOIN so2.items oi2 " +
           "WHERE oi1.variant.product.productId = :productId " +
           "AND oi2.variant.product.productId <> :productId " +
           "AND o.status IN :statuses " +
           "AND oi2.variant.product.status = :productStatus " +
           "GROUP BY oi2.variant.product.productId " +
           "ORDER BY COUNT(DISTINCT o) DESC")
    List<Object[]> findCoPurchasedProductIds(
            @Param("productId") Long productId,
            @Param("statuses") List<OrderStatus> statuses,
            @Param("productStatus") ProductStatus productStatus);

    /**
     * Sản phẩm cùng danh mục nhưng khác shop
     */
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s " +
           "JOIN FETCH p.category c " +
           "WHERE p.category.categoryId = :categoryId " +
           "AND p.shop.shopId <> :shopId " +
           "AND p.productId <> :excludeId " +
           "AND p.status = :productStatus " +
           "AND s.status = 'ACTIVE' " +
           "AND c.categoryStatus = 'ACTIVE'")
    List<Product> findSameCategoryDifferentShop(
            @Param("categoryId") Long categoryId,
            @Param("shopId") Long shopId,
            @Param("excludeId") Long excludeId,
            @Param("productStatus") ProductStatus productStatus);

    /**
     * Sản phẩm có tên tương tự nhưng khác shop (tìm biến thể từ shop khác)
     */
    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.shop s " +
           "JOIN FETCH p.category c " +
           "WHERE p.shop.shopId <> :shopId " +
           "AND p.productId <> :excludeId " +
           "AND p.status = :productStatus " +
           "AND s.status = 'ACTIVE' " +
           "AND c.categoryStatus = 'ACTIVE' " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByNameContainingDifferentShop(
            @Param("keyword") String keyword,
            @Param("shopId") Long shopId,
            @Param("excludeId") Long excludeId,
            @Param("productStatus") ProductStatus productStatus);

    /**
     * Lấy danh sách productId trong giỏ hàng của user
     */
    @Query("SELECT DISTINCT ci.variant.product.productId FROM CartItem ci WHERE ci.user.userId = :userId")
    List<Long> findProductIdsInUserCart(@Param("userId") Long userId);

    /**
     * Lấy danh sách productId trong đơn hàng đã hoàn thành của user
     */
    @Query("SELECT DISTINCT oi.variant.product.productId " +
           "FROM OrderItem oi " +
           "WHERE oi.shopOrder.order.user.userId = :userId " +
           "AND oi.shopOrder.order.status IN :statuses")
    List<Long> findRecentOrderProductIds(
            @Param("userId") Long userId,
            @Param("statuses") List<OrderStatus> statuses);

    /**
     * Tìm userId từ email (dùng cho personal recommendation)
     */
    @Query("SELECT u.userId FROM User u WHERE u.email = :email")
    Long findUserIdByEmail(@Param("email") String email);
}
