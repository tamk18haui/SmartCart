package com.gr6.SmartCart.common.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private int status;       // Mã trạng thái (vd: 200, 400, 500)
    private String message;   // Lời nhắn (vd: "Thành công", "Lỗi mật khẩu")
    private T data;           // Dữ liệu thật sự (Object, List,...)

    // Hàm tiện ích: Trả về khi API thành công, có kèm dữ liệu
    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }

    // Hàm tiện ích: Trả về khi API thành công, KHÔNG cần kèm dữ liệu (vd: Xóa thành công)
    public static <Void> BaseResponse<Void> successMessage(String message) {
        return BaseResponse.<Void>builder()
                .status(200)
                .message(message)
                .data(null)
                .build();
    }

    // Hàm tiện ích: Trả về khi API lỗi (Dùng trong GlobalExceptionHandler)
    public static <T> BaseResponse<T> error(int status, String message) {
        return BaseResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}