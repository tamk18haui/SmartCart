package com.gr6.SmartCart.modules.fulfillment.repository;

import com.gr6.SmartCart.common.domain.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý dữ liệu đánh giá sản phẩm.
 *
 * Lưu ý:
 * Review hiện tại KHÔNG còn gắn trực tiếp với Order nữa.
 * Review gắn với OrderItem.
 *
 * Luồng quan hệ đúng:
 * Review -> OrderItem -> ShopOrder -> Order
 *
 * Vì vậy tuyệt đối không dùng EntityGraph path "order".
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Lấy danh sách review của một sản phẩm để hiển thị ở màn chi tiết sản phẩm.
     *
     * Fetch sẵn:
     * - user: người đánh giá
     * - product/shop: sản phẩm và shop
     * - orderItem/variant: item đã mua và phân loại
     * - shopOrder/order: đơn shop và đơn cha
     */
    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "orderItem",
            "orderItem.variant",
            "orderItem.variant.product",
            "orderItem.shopOrder",
            "orderItem.shopOrder.order"
    })
    @Query("""
            SELECT r
            FROM Review r
            WHERE r.product.productId = :productId
            ORDER BY r.createdAt DESC
            """)
    List<Review> findByProductId(@Param("productId") Long productId);

    /**
     * Tìm review theo orderItemId.
     * Mỗi OrderItem chỉ được review một lần.
     */
    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "orderItem",
            "orderItem.variant",
            "orderItem.variant.product",
            "orderItem.shopOrder",
            "orderItem.shopOrder.order"
    })
    Optional<Review> findByOrderItem_OrderItemId(Long orderItemId);

    /**
     * Kiểm tra sản phẩm trong đơn đã được review chưa.
     */
    boolean existsByOrderItem_OrderItemId(Long orderItemId);

    /**
     * Lấy danh sách review của buyer hiện tại.
     */
    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "orderItem",
            "orderItem.variant",
            "orderItem.variant.product",
            "orderItem.shopOrder",
            "orderItem.shopOrder.order"
    })
    List<Review> findByUser_EmailOrderByCreatedAtDesc(String email);

    /**
     * Seller xem review của các sản phẩm thuộc shop mình.
     */
    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "orderItem",
            "orderItem.variant",
            "orderItem.variant.product",
            "orderItem.shopOrder",
            "orderItem.shopOrder.order"
    })
    List<Review> findByProduct_Shop_User_EmailOrderByCreatedAtDesc(String email);

    /**
     * Admin xem tất cả review.
     */
    @EntityGraph(attributePaths = {
            "user",
            "product",
            "product.shop",
            "orderItem",
            "orderItem.variant",
            "orderItem.variant.product",
            "orderItem.shopOrder",
            "orderItem.shopOrder.order"
    })
    List<Review> findAllByOrderByCreatedAtDesc();
}