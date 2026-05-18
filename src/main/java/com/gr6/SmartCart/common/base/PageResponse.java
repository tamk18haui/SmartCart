package com.gr6.SmartCart.common.base;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
@Builder
public class PageResponse<T> {
    private int currentPage;   // Trang hiện tại (Bắt đầu từ 1 cho Frontend dễ hiểu)
    private int pageSize;      // Số lượng trên 1 trang
    private int totalPages;    // Tổng số trang
    private long totalElements; // Tổng số bản ghi trong DB
    private List<T> data;      // Danh sách dữ liệu của trang đó

    // Hàm tiện ích: Chuyển đổi thẳng từ cục Page của Spring Boot sang format chuẩn
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent())
                .build();
    }
}
