package com.gr6.SmartCart.module_v2.user.service.Impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.domain.Address;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.modules.finance_core.repository.AddressRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.module_v2.user.dto.AddressRequestDTO;
import com.gr6.SmartCart.module_v2.user.dto.AddressResponseDTO;
import com.gr6.SmartCart.module_v2.user.service.AddressService;
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
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Chưa đăng nhập!"));
    }

    // --- HÀM ULTI: TẮT MẶC ĐỊNH CŨ ---
    private void unsetOldDefault(User user) {
        List<Address> addresses = addressRepository.findByUser(user);
        for (Address addr : addresses) {
            if (Boolean.TRUE.equals(addr.getIsDefault()) && !Boolean.TRUE.equals(addr.getIsDeleted())) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
    }

    @Override
    public BaseResponse<List<AddressResponseDTO>> getMyAddresses() {
        User user = getCurrentUser();
        List<AddressResponseDTO> response = addressRepository.findByUser(user).stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted())) // Chỉ lấy địa chỉ chưa xóa
                .sorted((a1, a2) -> Boolean.TRUE.equals(a2.getIsDefault()) ? 1 : -1) // Sắp xếp: Mặc định đẩy lên đầu tiên
                .map(a -> AddressResponseDTO.builder()
                        .addressId(a.getAddressId())
                        .receiverName(a.getReceiverName())
                        .receiverPhone(a.getReceiverPhone())
                        .fullAddress(a.getFullAddress())
                        .isDefault(a.getIsDefault())
                        .build())
                .collect(Collectors.toList());

        return BaseResponse.success(response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<AddressResponseDTO> createAddress(AddressRequestDTO request) {
        User user = getCurrentUser();
        List<Address> activeAddresses = addressRepository.findByUser(user).stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                .toList();

        // Nếu chưa có địa chỉ nào, bắt buộc cái đầu tiên phải là Mặc định
        boolean isFirst = activeAddresses.isEmpty();
        boolean wantDefault = Boolean.TRUE.equals(request.getIsDefault());
        boolean isDefault = isFirst || wantDefault;

        // Nếu cái mới này là mặc định, phải tắt cái cũ đi
        if (isDefault && !isFirst) {
            unsetOldDefault(user);
        }

        Address address = new Address();
        address.setUser(user);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setFullAddress(request.getFullAddress());
        address.setIsDefault(isDefault);
        address.setIsDeleted(false);

        Address savedAddress = addressRepository.save(address);

        return BaseResponse.success_data("Thêm địa chỉ thành công!", AddressResponseDTO.builder()
                .addressId(savedAddress.getAddressId())
                .receiverName(savedAddress.getReceiverName())
                .receiverPhone(savedAddress.getReceiverPhone())
                .fullAddress(savedAddress.getFullAddress())
                .isDefault(savedAddress.getIsDefault())
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<AddressResponseDTO> updateAddress(Long addressId, AddressRequestDTO request) {
        User user = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        // BẢO MẬT IDOR
        if (!address.getUser().getUserId().equals(user.getUserId()) || Boolean.TRUE.equals(address.getIsDeleted())) {
            return BaseResponse.error(403, "Bạn không có quyền sửa địa chỉ này!");
        }

        boolean wantDefault = Boolean.TRUE.equals(request.getIsDefault());

        // LOGIC SHOPEE 2: Đang là mặc định thì KHÔNG ĐƯỢC TẮT (Chỉ được phép bật cái khác lên mặc định)
        if (Boolean.TRUE.equals(address.getIsDefault()) && !wantDefault) {
            wantDefault = true; // Ép lại thành true
        }

        if (wantDefault && !Boolean.TRUE.equals(address.getIsDefault())) {
            unsetOldDefault(user); // Tắt các cái khác nếu biến cái này thành mặc định
        }

        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setFullAddress(request.getFullAddress());
        address.setIsDefault(wantDefault);

        Address updatedAddress = addressRepository.save(address);

        return BaseResponse.success_data("Cập nhật địa chỉ thành công!", AddressResponseDTO.builder()
                .addressId(updatedAddress.getAddressId())
                .receiverName(updatedAddress.getReceiverName())
                .receiverPhone(updatedAddress.getReceiverPhone())
                .fullAddress(updatedAddress.getFullAddress())
                .isDefault(updatedAddress.getIsDefault())
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<String> setDefaultAddress(Long addressId) {
        User user = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        if (!address.getUser().getUserId().equals(user.getUserId()) || Boolean.TRUE.equals(address.getIsDeleted())) {
            return BaseResponse.error(403, "Thao tác không hợp lệ!");
        }

        if (!Boolean.TRUE.equals(address.getIsDefault())) {
            unsetOldDefault(user);
            address.setIsDefault(true);
            addressRepository.save(address);
        }

        return BaseResponse.successMessage("Đã thiết lập địa chỉ mặc định!");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<String> deleteAddress(Long addressId) {
        User user = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        if (!address.getUser().getUserId().equals(user.getUserId()) || Boolean.TRUE.equals(address.getIsDeleted())) {
            return BaseResponse.error(403, "Không có quyền xóa địa chỉ này!");
        }

        // Xóa mềm (Soft Delete)
        address.setIsDeleted(true);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        address.setIsDefault(false);
        addressRepository.save(address);

        // LOGIC SHOPEE 3: Nếu xóa trúng địa chỉ Mặc định, đôn 1 địa chỉ cũ nhất lên làm Mặc định bù vào
        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUser(user).stream()
                    .filter(a -> !Boolean.TRUE.equals(a.getIsDeleted()))
                    .sorted(Comparator.comparing(Address::getAddressId)) // Cũ nhất (ID nhỏ nhất)
                    .toList();

            if (!remaining.isEmpty()) {
                Address fallbackDefault = remaining.get(0);
                fallbackDefault.setIsDefault(true);
                addressRepository.save(fallbackDefault);
            }
        }

        return BaseResponse.successMessage("Xóa địa chỉ thành công!");
    }
}