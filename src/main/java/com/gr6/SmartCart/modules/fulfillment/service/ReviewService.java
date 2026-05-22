package com.gr6.SmartCart.modules.fulfillment.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewCreateRequest;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewUpdateRequest;
import com.gr6.SmartCart.modules.fulfillment.dto.ReviewableOrderItemResponse;
import com.gr6.SmartCart.modules.fulfillment.dto.SellerReplyRequest;

import java.util.List;

public interface ReviewService {

    BaseResponse<List<ReviewableOrderItemResponse>> getMyReviewableItems();

    BaseResponse<List<ReviewResponse>> getMyReviews();

    BaseResponse<List<ReviewResponse>> getShopReviews();

    BaseResponse<ReviewResponse> createReview(ReviewCreateRequest request);

    BaseResponse<ReviewResponse> updateReview(Long reviewId, ReviewUpdateRequest request);

    BaseResponse<String> deleteReview(Long reviewId);

    BaseResponse<ReviewResponse> replyReview(Long reviewId, SellerReplyRequest request);
}