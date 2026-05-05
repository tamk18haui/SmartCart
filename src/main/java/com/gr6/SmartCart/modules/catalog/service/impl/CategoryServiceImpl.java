package com.gr6.SmartCart.modules.catalog.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Category;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.UserRole;
import com.gr6.SmartCart.modules.catalog.dto.CategoryRequest;
import com.gr6.SmartCart.modules.catalog.dto.CategoryResponse;
import com.gr6.SmartCart.modules.catalog.repository.CategoryRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.catalog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository; // Kéo UserRepository vào để check quyền

    @Override
    public BaseResponse<CategoryResponse> createCategory(CategoryRequest request) {
        // CHỐT CHẶN: Phải là Quản trị viên (ADMIN) mới được tạo danh mục hệ thống
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return BaseResponse.error(401, "Bạn chưa đăng nhập!");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if (user.getRole() != UserRole.ADMIN) {
            return BaseResponse.error(403, "Cảnh báo: Chỉ ADMIN mới có quyền tạo Danh mục!");
        }

        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            return BaseResponse.error(400, "Tên danh mục này đã tồn tại!");
        }

        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        category.setCategoryDescription(request.getCategoryDescription());
        category.setCategoryImageUrl(request.getCategoryImageUrl());
        category.setCategoryStatus(CategoryStatus.ACTIVE);

        Category savedCategory = categoryRepository.save(category);
        return BaseResponse.success_data("Tạo danh mục thành công!", CategoryResponse.fromEntity(savedCategory));
    }

    @Override
    public BaseResponse<List<CategoryResponse>> getAllCategories() {
        // Chỉ trả ra những danh mục đang ACTIVE
        List<CategoryResponse> responses = categoryRepository.findAll()
                .stream()
                .filter(c -> c.getCategoryStatus() == CategoryStatus.ACTIVE)
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
        return BaseResponse.success_data("Lấy danh sách thành công", responses);
    }
}