package com.gr6.SmartCart.common.exception;

import com.gr6.SmartCart.common.base.BaseResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<String> handleValidation(MethodArgumentNotValidException ex) {
        String msg = "Dữ liệu không hợp lệ";

        if (ex.getBindingResult().getFieldError() != null) {
            msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        }

        return BaseResponse.error(400, msg);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<String> handleConstraintViolation(ConstraintViolationException ex) {
        return BaseResponse.error(400, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public BaseResponse<String> handleDataIntegrity(DataIntegrityViolationException ex) {
        return BaseResponse.error(409, "Dữ liệu bị trùng hoặc vi phạm ràng buộc hệ thống!");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<String> handleBusinessException(RuntimeException ex) {
        return BaseResponse.error(400, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public BaseResponse<String> handleGlobalException(Exception ex) {
        return BaseResponse.error(500, "Lỗi hệ thống nội bộ, vui lòng thử lại sau!");
    }
    @ExceptionHandler(CustomException.class)
    public org.springframework.http.ResponseEntity<BaseResponse<String>> handleCustomException(CustomException ex) {
        // Dùng ResponseEntity để trả về đúng HTTP Status Code động (200, 400, 404...)
        // dựa vào ex.getCode() mà bạn set lúc throw lỗi.
        return org.springframework.http.ResponseEntity
                .status(ex.getCode())
                .body(BaseResponse.error(ex.getCode(), ex.getMessage()));
    }
}