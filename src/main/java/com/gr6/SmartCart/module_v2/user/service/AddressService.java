package com.gr6.SmartCart.module_v2.user.service;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.module_v2.user.dto.AddressRequestDTO;
import com.gr6.SmartCart.module_v2.user.dto.AddressResponseDTO;

import java.util.List;

public interface AddressService {
    BaseResponse<List<AddressResponseDTO>> getMyAddresses();
    BaseResponse<AddressResponseDTO> createAddress(AddressRequestDTO request);
    BaseResponse<AddressResponseDTO> updateAddress(Long addressId, AddressRequestDTO request);
    BaseResponse<String> deleteAddress(Long addressId);
    BaseResponse<String> setDefaultAddress(Long addressId);
}