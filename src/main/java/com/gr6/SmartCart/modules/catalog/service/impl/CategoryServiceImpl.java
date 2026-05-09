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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // SÁNG THÊM VÀO ĐÂY: Hàm check quyên ADMIN gọn gàng hơn
    private void verifyAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Bạn chưa đăng nhập!");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        if (user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Cảnh báo: Chỉ ADMIN mới có quyền thao tác Danh mục!");
        }
    }

    @Override
    @Transactional
    public BaseResponse<CategoryResponse> createCategory(CategoryRequest request) {
        verifyAdminRole();

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
        List<CategoryResponse> responses = categoryRepository.findAll()
                .stream()
                .filter(c -> c.getCategoryStatus() == CategoryStatus.ACTIVE)
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
        return BaseResponse.success_data("Lấy danh sách thành công", responses);
    }

    // SÁNG THÊM VÀO ĐÂY: Logic Cập nhật Danh mục
    @Override
    @Transactional
    public BaseResponse<CategoryResponse> updateCategory(Long id, CategoryRequest request) {
        verifyAdminRole();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        if (!category.getCategoryName().equals(request.getCategoryName()) &&
                categoryRepository.existsByCategoryName(request.getCategoryName())) {
            return BaseResponse.error(400, "Tên danh mục này đã có trên hệ thống!");
        }

        category.setCategoryName(request.getCategoryName());
        category.setCategoryDescription(request.getCategoryDescription());
        category.setCategoryImageUrl(request.getCategoryImageUrl());

        categoryRepository.save(category);
        return BaseResponse.success_data("Cập nhật danh mục thành công!", CategoryResponse.fromEntity(category));
    }

    // SÁNG THÊM VÀO ĐÂY: Logic Ẩn/Hiện Danh mục
    @Override
    @Transactional
    public BaseResponse<String> toggleCategoryStatus(Long id) {
        verifyAdminRole();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        if (category.getCategoryStatus() == CategoryStatus.ACTIVE) {
            category.setCategoryStatus(CategoryStatus.HIDDEN);
        } else {
            category.setCategoryStatus(CategoryStatus.ACTIVE);
        }

        categoryRepository.save(category);
        return BaseResponse.successMessage("Đã thay đổi trạng thái danh mục thành " + category.getCategoryStatus().name());
    }
}