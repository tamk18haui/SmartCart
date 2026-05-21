package com.gr6.SmartCart.module_v3.withdraw.dto;

import lombok.Data;

@Data
public class AdminWithdrawDecisionRequest {
    private String adminNote;
    private String transferCode;
}