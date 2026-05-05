package com.gr6.SmartCart.common.exception;

import com.gr6.SmartCart.common.base.BaseResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
// hàm bắt lỗi dùng chung
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Lỗi Validation (vd: @NotBlank, @Min...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<String> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        return BaseResponse.error(400, msg);
    }

    // Lỗi nghiệp vụ (Voucher hết hạn, Sai mã...) -> Trả về 400
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<String> handleBusinessException(RuntimeException ex) {
        return BaseResponse.error(400, ex.getMessage());
    }

    // Lỗi hệ thống thực sự (Null Pointer, Database sập...) -> Trả về 500
    @ExceptionHandler(Exception.class)
    public BaseResponse<String> handleGlobalException(Exception ex) {
        return BaseResponse.error(500, "Lỗi hệ thống nội bộ, vui lòng thử lại sau!");
    }
}
