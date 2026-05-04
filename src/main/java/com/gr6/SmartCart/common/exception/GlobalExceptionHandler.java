package com.gr6.SmartCart.common.exception;

import com.gr6.SmartCart.common.base.BaseResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Lấy thông báo lỗi đầu tiên mà bạn đã viết trong DTO (vd: "Mật khẩu phải có...")
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        return BaseResponse.error(400, errorMessage);
    }
}
