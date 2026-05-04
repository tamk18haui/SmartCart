package com.gr6.SmartCart.modules.identity.service.impl;

import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.modules.identity.dto.ShopManagerRequest;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import com.gr6.SmartCart.modules.identity.service.ShopManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopManagerServiceImpl implements ShopManagerService {
    private final ShopRepository shopRepository;

    @Override
    public String updateShop(Integer id, ShopManagerRequest request) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy shop"));

        shop.setShopName(request.getShopName());
        shop.setDescription(request.getDescription());
        shop.setPickupAddress(request.getPickupAddress());

        shopRepository.save(shop);
        return "Cập nhật thông tin shop thành công!";
    }
}