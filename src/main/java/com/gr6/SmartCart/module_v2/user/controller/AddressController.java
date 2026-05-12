package com.gr6.SmartCart.module_v2.user.controller;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v2.user.dto.AddressRequestDTO;
import com.gr6.SmartCart.module_v2.user.dto.AddressResponseDTO;
import com.gr6.SmartCart.module_v2.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/customer/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // Xem danh sách địa chỉ (Đã tự xếp Mặc định lên đầu)
    @GetMapping
    public BaseResponse<List<AddressResponseDTO>> getMyAddresses() {
        return addressService.getMyAddresses();
    }

    // Thêm địa chỉ mới
    @PostMapping
    public BaseResponse<AddressResponseDTO> createAddress(@Valid @RequestBody AddressRequestDTO request) {
        return addressService.createAddress(request);
    }

    // Sửa địa chỉ
    @PutMapping("/{id}")
    public BaseResponse<AddressResponseDTO> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequestDTO request) {
        return addressService.updateAddress(id, request);
    }

    // Bấm xóa
    @DeleteMapping("/{id}")
    public BaseResponse<String> deleteAddress(@PathVariable Long id) {
        return addressService.deleteAddress(id);
    }

    // Bấm nút "Thiết lập làm mặc định"
    @PutMapping("/{id}/set-default")
    public BaseResponse<String> setDefaultAddress(@PathVariable Long id) {
        return addressService.setDefaultAddress(id);
    }
}