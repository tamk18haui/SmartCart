package com.gr6.SmartCart.modules.storefront.service;

import com.gr6.SmartCart.common.domain.CartItem;
import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.storefront.dto.CartDetailResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.CartItemRequest;
import com.gr6.SmartCart.modules.storefront.dto.CartItemResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.ChangeVariantRequest;
import com.gr6.SmartCart.modules.storefront.dto.ShopCartDTO;
import com.gr6.SmartCart.modules.storefront.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Chưa đăng nhập hệ thống!"));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0!");
        }
    }

    private void validatePurchasableVariant(ProductVariant variant) {
        if (variant == null || variant.getStatus() != VariantStatus.ACTIVE) {
            throw new RuntimeException("Phân loại sản phẩm hiện không khả dụng!");
        }

        Product product = variant.getProduct();
        if (product == null || product.getStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Sản phẩm hiện không khả dụng!");
        }

        if (product.getCategory() == null || product.getCategory().getCategoryStatus() != CategoryStatus.ACTIVE) {
            throw new RuntimeException("Danh mục sản phẩm hiện không khả dụng!");
        }

        Shop shop = product.getShop();
        if (shop == null || shop.getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop hiện không khả dụng!");
        }
    }

    private String buildVariantAttributes(ProductVariant variant) {
        if (variant.getVariantOptionValues() == null || variant.getVariantOptionValues().isEmpty()) {
            return "";
        }
        return variant.getVariantOptionValues().stream()
                .filter(vov -> vov.getOptionValue() != null)
                .map(vov -> vov.getOptionValue().getValue())
                .collect(Collectors.joining(", "));
    }

    private Double getVariantPrice(ProductVariant variant) {
        if (variant.getPrice() != null) {
            return variant.getPrice().doubleValue();
        }
        if (variant.getProduct() != null && variant.getProduct().getBasePrice() != null) {
            return variant.getProduct().getBasePrice().doubleValue();
        }
        return 0D;
    }

    private CartItemResponseDTO toCartItemResponse(CartItem cartItem) {
        ProductVariant variant = cartItem.getVariant();
        Product product = variant.getProduct();

        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setCartItemId(cartItem.getCartItemId());
        dto.setVariantId(variant.getVariantId());
        dto.setProductId(product.getProductId());
        dto.setVariantSku(variant.getSku());
        dto.setProductName(product.getName());
        dto.setVariantAttributes(buildVariantAttributes(variant));
        dto.setImageUrl(variant.getImageUrl() != null && !variant.getImageUrl().isBlank()
                ? variant.getImageUrl()
                : product.getImageUrls());
        dto.setPrice(getVariantPrice(variant));
        dto.setQuantity(cartItem.getQuantity());
        dto.setMaxQuantity(variant.getStockQuantity());
        return dto;
    }

    @Transactional(readOnly = true)
    public CartDetailResponseDTO getCartDetails() {
        User user = getCurrentUser();
        List<CartItem> items = cartItemRepository.findByUser_UserId(user.getUserId());

        List<CartItem> validItems = items.stream()
                .filter(item -> {
                    try {
                        validatePurchasableVariant(item.getVariant());
                        return item.getQuantity() != null && item.getQuantity() > 0;
                    } catch (RuntimeException ex) {
                        return false;
                    }
                })
                .toList();

        Map<Shop, List<CartItem>> groupedByShop = validItems.stream()
                .collect(Collectors.groupingBy(item -> item.getVariant().getProduct().getShop()));

        List<ShopCartDTO> shopDtos = new ArrayList<>();
        int totalItems = 0;
        double totalPrice = 0D;

        for (Map.Entry<Shop, List<CartItem>> entry : groupedByShop.entrySet()) {
            Shop shop = entry.getKey();
            List<CartItem> shopItems = entry.getValue();

            List<CartItemResponseDTO> itemDtos = shopItems.stream()
                    .map(this::toCartItemResponse)
                    .toList();

            double shopSubtotal = itemDtos.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();

            int shopQuantity = itemDtos.stream()
                    .mapToInt(CartItemResponseDTO::getQuantity)
                    .sum();

            ShopCartDTO shopDto = new ShopCartDTO();
            shopDto.setShopId(shop.getShopId());
            shopDto.setShopName(shop.getShopName());
            shopDto.setItems(itemDtos);
            shopDto.setShopSubtotal(shopSubtotal);
            shopDtos.add(shopDto);

            totalItems += shopQuantity;
            totalPrice += shopSubtotal;
        }

        CartDetailResponseDTO response = new CartDetailResponseDTO();
        response.setShops(shopDtos);
        response.setTotalItems(totalItems);
        response.setTotalPrice(totalPrice);
        response.setIsEmpty(validItems.isEmpty());
        return response;
    }

    @Transactional
    public String addToCart(CartItemRequest request) {
        validateQuantity(request.getQuantity());
        User user = getCurrentUser();

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân loại sản phẩm!"));
        validatePurchasableVariant(variant);

        Optional<CartItem> existingItemOpt = cartItemRepository
                .findByUser_UserIdAndVariant_VariantId(user.getUserId(), variant.getVariantId());

        int newQuantity = request.getQuantity();
        CartItem cartItem;
        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            newQuantity += cartItem.getQuantity();
        } else {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setVariant(variant);
        }

        if (variant.getStockQuantity() == null || newQuantity > variant.getStockQuantity()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho hiện có!");
        }

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
        return "Đã thêm sản phẩm vào giỏ hàng!";
    }

    @Transactional
    public String updateQuantity(CartItemRequest request) {
        validateQuantity(request.getQuantity());
        User user = getCurrentUser();

        CartItem cartItem = cartItemRepository
                .findByUser_UserIdAndVariant_VariantId(user.getUserId(), request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong giỏ hàng!"));

        ProductVariant variant = cartItem.getVariant();
        validatePurchasableVariant(variant);

        if (variant.getStockQuantity() == null || request.getQuantity() > variant.getStockQuantity()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho hiện có!");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        return "Cập nhật số lượng thành công!";
    }

    @Transactional
    public String changeVariant(ChangeVariantRequest request) {
        if (request == null || request.getCartItemId() == null || request.getNewVariantId() == null) {
            throw new RuntimeException("Thiếu thông tin đổi phân loại sản phẩm!");
        }

        User user = getCurrentUser();
        CartItem cartItem = cartItemRepository
                .findByCartItemIdAndUser_UserId(request.getCartItemId(), user.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng!"));

        ProductVariant oldVariant = cartItem.getVariant();
        ProductVariant newVariant = variantRepository.findById(request.getNewVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân loại sản phẩm mới!"));

        validatePurchasableVariant(newVariant);

        if (!oldVariant.getProduct().getProductId().equals(newVariant.getProduct().getProductId())) {
            throw new RuntimeException("Không thể đổi sang phân loại của sản phẩm khác!");
        }

        if (newVariant.getStockQuantity() == null || cartItem.getQuantity() > newVariant.getStockQuantity()) {
            throw new RuntimeException("Phân loại mới không đủ tồn kho!");
        }

        Optional<CartItem> duplicateItemOpt = cartItemRepository
                .findByUser_UserIdAndVariant_VariantId(user.getUserId(), newVariant.getVariantId());

        if (duplicateItemOpt.isPresent() && !duplicateItemOpt.get().getCartItemId().equals(cartItem.getCartItemId())) {
            CartItem duplicateItem = duplicateItemOpt.get();
            int mergedQuantity = duplicateItem.getQuantity() + cartItem.getQuantity();
            if (mergedQuantity > newVariant.getStockQuantity()) {
                throw new RuntimeException("Tổng số lượng sau khi đổi vượt quá tồn kho!");
            }
            duplicateItem.setQuantity(mergedQuantity);
            cartItemRepository.save(duplicateItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setVariant(newVariant);
            cartItemRepository.save(cartItem);
        }

        return "Đổi phân loại sản phẩm trong giỏ hàng thành công!";
    }

    @Transactional
    public String removeFromCart(Long variantId) {
        User user = getCurrentUser();
        CartItem cartItem = cartItemRepository
                .findByUser_UserIdAndVariant_VariantId(user.getUserId(), variantId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong giỏ hàng!"));
        cartItemRepository.delete(cartItem);
        return "Đã xóa sản phẩm khỏi giỏ hàng!";
    }
}