package com.gr6.SmartCart.module_v3.withdraw.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class WithdrawCreateRequest {

    @NotNull(message = "Số tiền rút không được để trống")
    @Min(value = 10000, message = "Số tiền rút tối thiểu là 10.000đ")
    private Long amount;

    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;

    @NotBlank(message = "Số tài khoản không được để trống")
    private String bankAccountNumber;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    private String bankAccountHolder;

    private String sellerNote;
}