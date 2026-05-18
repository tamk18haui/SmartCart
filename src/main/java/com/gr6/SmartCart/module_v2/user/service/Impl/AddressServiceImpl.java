package com.gr6.SmartCart.module_v2.user.service.Impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Address;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.module_v2.user.dto.AddressRequestDTO;
import com.gr6.SmartCart.module_v2.user.dto.AddressResponseDTO;
import com.gr6.SmartCart.module_v2.user.service.AddressService;
import com.gr6.SmartCart.modules.finance_core.repository.AddressRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
    }

    private AddressResponseDTO toResponse(Address address) {
        return AddressResponseDTO.builder()
                .addressId(address.getAddressId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .fullAddress(address.getFullAddress())
                .isDefault(address.getIsDefault())
                .build();
    }

    @Override
    public BaseResponse<List<AddressResponseDTO>> getMyAddresses() {
        User user = getCurrentUser();

        List<AddressResponseDTO> response = addressRepository
                .findByUserAndIsDeletedFalse(user)
                .stream()
                .sorted(
                        Comparator.comparing(
                                Address::getIsDefault,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                )
                .map(this::toResponse)
                .collect(Collectors.toList());

        return BaseResponse.success(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<AddressResponseDTO> createAddress(AddressRequestDTO request) {
        User user = getCurrentUser();

        List<Address> activeAddresses = addressRepository.findByUserAndIsDeletedFalse(user);

        boolean isFirst = activeAddresses.isEmpty();
        boolean wantDefault = Boolean.TRUE.equals(request.getIsDefault());
        boolean isDefault = isFirst || wantDefault;

        if (isDefault && !isFirst) {
            addressRepository.clearDefaultAddress(user);
        }

        Address address = new Address();
        address.setUser(user);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setFullAddress(request.getFullAddress());
        address.setIsDefault(isDefault);
        address.setIsDeleted(false);

        Address savedAddress = addressRepository.save(address);

        return BaseResponse.success_data(
                "Thêm địa chỉ thành công!",
                toResponse(savedAddress)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<AddressResponseDTO> updateAddress(Long addressId, AddressRequestDTO request) {
        User user = getCurrentUser();

        Address address = addressRepository
                .findByAddressIdAndUserAndIsDeletedFalse(addressId, user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        boolean wantDefault = Boolean.TRUE.equals(request.getIsDefault());

        List<Address> activeAddresses = addressRepository.findByUserAndIsDeletedFalse(user);
        boolean onlyOneAddress = activeAddresses.size() == 1;

        if (onlyOneAddress) {
            wantDefault = true;
        }

        if (wantDefault && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultAddress(user);
        }

        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setFullAddress(request.getFullAddress());
        address.setIsDefault(wantDefault);

        Address updatedAddress = addressRepository.save(address);

        return BaseResponse.success_data(
                "Cập nhật địa chỉ thành công!",
                toResponse(updatedAddress)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<String> setDefaultAddress(Long addressId) {
        User user = getCurrentUser();

        Address address = addressRepository
                .findByAddressIdAndUserAndIsDeletedFalse(addressId, user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        if (!Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.clearDefaultAddress(user);
            address.setIsDefault(true);
            addressRepository.save(address);
        }

        return BaseResponse.successMessage("Đã thiết lập địa chỉ mặc định!");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<String> deleteAddress(Long addressId) {
        User user = getCurrentUser();

        Address address = addressRepository
                .findByAddressIdAndUserAndIsDeletedFalse(addressId, user)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        address.setIsDeleted(true);
        address.setIsDefault(false);
        addressRepository.save(address);

        if (wasDefault) {
            addressRepository
                    .findFirstByUserAndIsDeletedFalseOrderByAddressIdAsc(user)
                    .ifPresent(fallbackDefault -> {
                        fallbackDefault.setIsDefault(true);
                        addressRepository.save(fallbackDefault);
                    });
        }

        return BaseResponse.successMessage("Xóa địa chỉ thành công!");
    }
}