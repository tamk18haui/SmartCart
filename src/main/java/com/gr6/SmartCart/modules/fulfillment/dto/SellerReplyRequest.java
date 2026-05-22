package com.gr6.SmartCart.modules.fulfillment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerReplyRequest {

    @NotBlank(message = "Vui lòng nhập nội dung phản hồi")
    @Size(max = 2000, message = "Nội dung phản hồi không được vượt quá 2000 ký tự")
    private String reply;
}