package com.gr6.SmartCart.modules.fulfillment.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.OrderItem;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Review;
import com.gr6.SmartCart.common.domain.ShopOrder;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.modules.finance_core.repository.OrderItemRepository;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewCreateRequest;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewUpdateRequest;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewableOrderItemResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.SellerReplyRequest;
import com.gr6.SmartCart.modules.fulfillment.repository.ReviewRepository;
import com.gr6.SmartCart.modules.fulfillment.service.ReviewService;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<ReviewableOrderItemResponse>> getMyReviewableItems() {
        User user = getCurrentUser();

        if (user.getRole() != UserRole.BUYER) {
            throw new RuntimeException("Chỉ người mua mới được xem danh sách sản phẩm cần đánh giá!");
        }

        List<ReviewableOrderItemResponse> response = orderItemRepository
                .findReviewableItemsByBuyerEmail(user.getEmail(), OrderStatus.COMPLETED)
                .stream()
                .map(this::mapReviewableItem)
                .toList();

        return BaseResponse.success_data("Lấy danh sách sản phẩm có thể đánh giá thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<ReviewResponse>> getMyReviews() {
        User user = getCurrentUser();

        List<ReviewResponse> response = reviewRepository
                .findByUser_EmailOrderByCreatedAtDesc(user.getEmail())
                .stream()
                .map(this::mapReview)
                .toList();

        return BaseResponse.success_data("Lấy danh sách đánh giá của tôi thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<List<ReviewResponse>> getShopReviews() {
        User user = getCurrentUser();

        List<Review> reviews;

        if (user.getRole() == UserRole.ADMIN) {
            reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
        } else if (user.getRole() == UserRole.SELLER) {
            reviews = reviewRepository.findByProduct_Shop_User_EmailOrderByCreatedAtDesc(user.getEmail());
        } else {
            throw new RuntimeException("Chỉ seller mới được xem phản hồi khách hàng của shop!");
        }

        List<ReviewResponse> response = reviews.stream()
                .map(this::mapReview)
                .toList();

        return BaseResponse.success_data("Lấy danh sách phản hồi khách hàng thành công", response);
    }

    @Override
    @Transactional
    public BaseResponse<ReviewResponse> createReview(ReviewCreateRequest request) {
        User user = getCurrentUser();

        if (user.getRole() != UserRole.BUYER) {
            throw new RuntimeException("Chỉ người mua mới được đánh giá sản phẩm!");
        }

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong đơn hàng!"));

        ShopOrder shopOrder = orderItem.getShopOrder();

        if (shopOrder == null || shopOrder.getOrder() == null) {
            throw new RuntimeException("Dữ liệu đơn hàng không hợp lệ!");
        }

        if (!shopOrder.getOrder().getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá sản phẩm này!");
        }

        if (shopOrder.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã hoàn thành!");
        }

        if (reviewRepository.existsByOrderItem_OrderItemId(orderItem.getOrderItemId())) {
            throw new RuntimeException("Sản phẩm này đã được đánh giá rồi!");
        }

        ProductVariant variant = orderItem.getVariant();

        if (variant == null || variant.getProduct() == null) {
            throw new RuntimeException("Sản phẩm không tồn tại!");
        }

        Review review = new Review();
        review.setOrder(shopOrder.getOrder());
        review.setOrderItem(orderItem);
        review.setUser(user);
        review.setProduct(variant.getProduct());
        review.setRating(request.getRating());
        review.setComment(normalizeText(request.getComment()));
        review.setImageUrl(normalizeText(request.getImageUrl()));

        Review savedReview = reviewRepository.save(review);

        return BaseResponse.success_data("Đánh giá sản phẩm thành công", mapReview(savedReview));
    }

    @Override
    @Transactional
    public BaseResponse<ReviewResponse> updateReview(Long reviewId, ReviewUpdateRequest request) {
        User user = getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá!"));

        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bạn không có quyền sửa đánh giá này!");
        }

        review.setRating(request.getRating());
        review.setComment(normalizeText(request.getComment()));
        review.setImageUrl(normalizeText(request.getImageUrl()));

        Review savedReview = reviewRepository.save(review);

        return BaseResponse.success_data("Cập nhật đánh giá thành công", mapReview(savedReview));
    }

    @Override
    @Transactional
    public BaseResponse<String> deleteReview(Long reviewId) {
        User user = getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá!"));

        boolean isOwner = review.getUser().getUserId().equals(user.getUserId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này!");
        }

        reviewRepository.delete(review);

        return BaseResponse.successMessage("Xóa đánh giá thành công");
    }

    @Override
    @Transactional
    public BaseResponse<ReviewResponse> replyReview(Long reviewId, SellerReplyRequest request) {
        User user = getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá!"));

        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        boolean isSellerOwner = review.getProduct() != null
                && review.getProduct().getShop() != null
                && review.getProduct().getShop().getUser() != null
                && review.getProduct().getShop().getUser().getUserId().equals(user.getUserId());

        if (!isAdmin && !isSellerOwner) {
            throw new RuntimeException("Bạn không có quyền phản hồi đánh giá này!");
        }

        review.setSellerReply(request.getReply().trim());
        review.setRepliedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        return BaseResponse.success_data("Phản hồi khách hàng thành công", mapReview(savedReview));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }

        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    }

    private ReviewableOrderItemResponse mapReviewableItem(OrderItem item) {
        ProductVariant variant = item.getVariant();
        Product product = variant != null ? variant.getProduct() : null;
        ShopOrder shopOrder = item.getShopOrder();

        return ReviewableOrderItemResponse.builder()
                .orderId(shopOrder != null && shopOrder.getOrder() != null
                        ? shopOrder.getOrder().getOrderId()
                        : null)
                .shopOrderId(shopOrder != null ? shopOrder.getShopOrderId() : null)
                .orderItemId(item.getOrderItemId())

                .productId(product != null ? product.getProductId() : null)
                .productName(product != null ? product.getName() : null)
                .productImageUrl(resolveProductImage(product, variant))

                .variantId(variant != null ? variant.getVariantId() : null)
                .variantSku(variant != null ? variant.getSku() : null)

                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())

                .shopId(product != null && product.getShop() != null
                        ? product.getShop().getShopId()
                        : null)
                .shopName(product != null && product.getShop() != null
                        ? product.getShop().getShopName()
                        : null)
                .build();
    }

    private ReviewResponse mapReview(Review review) {
        Product product = review.getProduct();
        ProductVariant variant = review.getOrderItem() != null ? review.getOrderItem().getVariant() : null;
        ShopOrder shopOrder = review.getOrderItem() != null ? review.getOrderItem().getShopOrder() : null;

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())

                .orderId(review.getOrder() != null ? review.getOrder().getOrderId() : null)
                .shopOrderId(shopOrder != null ? shopOrder.getShopOrderId() : null)
                .orderItemId(review.getOrderItem() != null ? review.getOrderItem().getOrderItemId() : null)

                .productId(product != null ? product.getProductId() : null)
                .productName(product != null ? product.getName() : null)
                .productImageUrl(resolveProductImage(product, variant))

                .variantId(variant != null ? variant.getVariantId() : null)
                .variantSku(variant != null ? variant.getSku() : null)

                .buyerId(review.getUser() != null ? review.getUser().getUserId() : null)
                .buyerName(review.getUser() != null ? review.getUser().getFullName() : "Người dùng SmartCart")

                .rating(review.getRating())
                .comment(review.getComment())
                .imageUrl(review.getImageUrl())

                .sellerReply(review.getSellerReply())

                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .repliedAt(review.getRepliedAt())
                .build();
    }

    private String resolveProductImage(Product product, ProductVariant variant) {
        if (variant != null && !isBlank(variant.getImageUrl())) {
            return variant.getImageUrl();
        }

        if (product == null || isBlank(product.getImageUrls())) {
            return null;
        }

        return product.getImageUrls().split(",")[0].trim();
    }

    private String normalizeText(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}