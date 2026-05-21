package com.gr6.SmartCart.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomException extends RuntimeException {

    private int code;
    private String message;

    // Constructor chỉ nhận message (Mặc định mã lỗi là 400 - Bad Request)
    public CustomException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    // Constructor nhận cả mã lỗi (code) và message (Giống như mình dùng ở bài trước)
    public CustomException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}