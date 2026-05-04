package com.gr6.SmartCart.modules.finance_core.service.impl;

import com.gr6.SmartCart.modules.finance_core.repository.AddressRepository;
import com.gr6.SmartCart.modules.finance_core.repository.OrderItemRepository;
import com.gr6.SmartCart.modules.finance_core.repository.OrderRepository;
import com.gr6.SmartCart.modules.finance_core.repository.ShopOrderRepository;
import com.gr6.SmartCart.modules.finance_core.service.OrderService;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShopRepository shopRepository;

}
