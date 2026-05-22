package com.gr6.SmartCart.modules.fulfillment.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewCreateRequest;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewUpdateRequest;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewableOrderItemResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.SellerReplyRequest;
import com.gr6.SmartCart.modules.fulfillment.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/eligible")
    public BaseResponse<List<ReviewableOrderItemResponse>> getMyReviewableItems() {
        return reviewService.getMyReviewableItems();
    }

    @GetMapping("/my")
    public BaseResponse<List<ReviewResponse>> getMyReviews() {
        return reviewService.getMyReviews();
    }

    @GetMapping("/shop")
    public BaseResponse<List<ReviewResponse>> getShopReviews() {
        return reviewService.getShopReviews();
    }

    @PostMapping
    public BaseResponse<ReviewResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return reviewService.createReview(request);
    }

    @PutMapping("/{reviewId}")
    public BaseResponse<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return reviewService.updateReview(reviewId, request);
    }

    @DeleteMapping("/{reviewId}")
    public BaseResponse<String> deleteReview(@PathVariable Long reviewId) {
        return reviewService.deleteReview(reviewId);
    }

    @PatchMapping("/{reviewId}/reply")
    public BaseResponse<ReviewResponse> replyReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody SellerReplyRequest request
    ) {
        return reviewService.replyReview(reviewId, request);
    }
}