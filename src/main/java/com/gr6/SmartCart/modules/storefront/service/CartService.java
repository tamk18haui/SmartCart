package com.gr6.SmartCart.modules.storefront.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gr6.SmartCart.common.domain.CartItem;
import com.gr6.SmartCart.common.domain.ProductVariant;
import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.modules.catalog.repository.ProductVariantRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import com.gr6.SmartCart.modules.storefront.dto.CartDetailResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.CartItemRequest;
import com.gr6.SmartCart.modules.storefront.dto.CartItemResponseDTO;
import com.gr6.SmartCart.modules.storefront.dto.ChangeVariantRequest;
import com.gr6.SmartCart.modules.storefront.dto.ShopCartDTO;
import com.gr6.SmartCart.modules.storefront.repository.CartItemRepository;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User chưa đăng nhập");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user trong DB"))
                .getUserId();
    }

    // ==========================================
    // 1. API LẤY GIỎ HÀNG (GOM NHÓM THEO SHOP NHƯ SHOPEE)
    // ==========================================
    public CartDetailResponseDTO getCartDetails() {
        Long currentUserId = getCurrentUserId();
        List<CartItem> cartItems = cartItemRepository.findByUser_UserId(currentUserId);

        CartDetailResponseDTO response = new CartDetailResponseDTO();
        if (cartItems == null || cartItems.isEmpty()) {
            response.setShops(new ArrayList<>());
            response.setTotalItems(0);
            response.setTotalPrice(0.0);
            response.setIsEmpty(true);
            return response;
        }

        // Gom nhóm các CartItem theo Shop
        Map<Shop, List<CartItem>> groupedByShop = cartItems.stream()
                .filter(item -> item.getVariant() != null && item.getVariant().getProduct() != null)
                .collect(Collectors.groupingBy(item -> item.getVariant().getProduct().getShop()));

        List<ShopCartDTO> shopCartDTOs = new ArrayList<>();
        int totalItems = 0;
        double totalPrice = 0.0;

        for (Map.Entry<Shop, List<CartItem>> entry : groupedByShop.entrySet()) {
            Shop shop = entry.getKey();
            ShopCartDTO shopCart = new ShopCartDTO();
            shopCart.setShopId(shop.getShopId());
            shopCart.setShopName(shop.getShopName());

            List<CartItemResponseDTO> itemDTOs = new ArrayList<>();
            double shopSubtotal = 0.0;

            for (CartItem item : entry.getValue()) {
                CartItemResponseDTO itemDTO = mapToCartItemDTO(item);
                itemDTOs.add(itemDTO);
                shopSubtotal += itemDTO.getPrice() * itemDTO.getQuantity();
                totalItems += itemDTO.getQuantity();
            }

            shopCart.setItems(itemDTOs);
            shopCart.setShopSubtotal(shopSubtotal);
            shopCartDTOs.add(shopCart);
            totalPrice += shopSubtotal;
        }

        response.setShops(shopCartDTOs);
        response.setTotalItems(totalItems);
        response.setTotalPrice(totalPrice);
        response.setIsEmpty(false);

        return response;
    }

    // Hàm chuyển đổi ánh xạ Entity sang DTO và nối chuỗi biến thể
    private CartItemResponseDTO mapToCartItemDTO(CartItem item) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setCartItemId(item.getCartItemId());
        dto.setQuantity(item.getQuantity());
        
        ProductVariant variant = item.getVariant();
        if (variant != null) {
            dto.setVariantId(variant.getVariantId());
            dto.setVariantSku(variant.getSku());
            dto.setPrice(variant.getPrice() != null ? variant.getPrice().doubleValue() : 0.0);
            dto.setMaxQuantity(variant.getStockQuantity()); // Tồn kho để Frontend hiển thị hoặc chặn bấm '+'

            // Lấy chuỗi mô tả biến thể (VD: "Da dầu 236ml")
            String variantStr = "";
            if (variant.getVariantOptionValues() != null && !variant.getVariantOptionValues().isEmpty()) {
                variantStr = variant.getVariantOptionValues().stream()
                        .map(v -> v.getOptionValue().getValue())
                        .collect(Collectors.joining(", "));
            }
            dto.setVariantAttributes(variantStr);

            if (variant.getProduct() != null) {
                dto.setProductId(variant.getProduct().getProductId());
                dto.setProductName(variant.getProduct().getName());
                
                // Lấy ảnh biến thể, nếu không có thì lấy ảnh chính của sản phẩm
                if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                     dto.setImageUrl(variant.getImageUrl());
                } else {
                    String imageUrls = variant.getProduct().getImageUrls();
                    if (imageUrls != null && !imageUrls.isEmpty()) {
                        dto.setImageUrl(imageUrls.split(",")[0].trim());
                    }
                }
            }
        }
        return dto;
    }

    // ==========================================
    // 2. CÁC API THÊM, SỬA, XÓA CƠ BẢN
    // ==========================================
    @Transactional
    public String addToCart(CartItemRequest request) {
        if (request.getQuantity() <= 0) throw new RuntimeException("Số lượng phải lớn hơn 0");

        Long currentUserId = getCurrentUserId();
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân loại sản phẩm này"));

        CartItem existingItem = cartItemRepository.findByUser_UserIdAndVariant_VariantId(currentUserId, request.getVariantId()).orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > variant.getStockQuantity()) throw new RuntimeException("Số lượng vượt quá tồn kho");
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
            return "Đã cập nhật số lượng sản phẩm trong giỏ.";
        } else {
            if (request.getQuantity() > variant.getStockQuantity()) throw new RuntimeException("Số lượng vượt quá tồn kho");
            CartItem newItem = new CartItem();
            User user = new User();
            user.setUserId(currentUserId);
            newItem.setUser(user);
            newItem.setVariant(variant);
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
            return "Đã thêm mới sản phẩm vào giỏ hàng.";
        }
    }

    @Transactional
    public String updateQuantity(CartItemRequest request) {
        if (request.getQuantity() <= 0) throw new RuntimeException("Số lượng phải lớn hơn 0");
        Long currentUserId = getCurrentUserId();
        CartItem item = cartItemRepository.findByUser_UserIdAndVariant_VariantId(currentUserId, request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));
        
        if (request.getQuantity() > item.getVariant().getStockQuantity()) {
            throw new RuntimeException("Số lượng cập nhật vượt quá tồn kho");
        }
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return "Cập nhật số lượng thành công";
    }

    @Transactional
    public String removeFromCart(Long variantId) {
        Long currentUserId = getCurrentUserId();
        CartItem item = cartItemRepository.findByUser_UserIdAndVariant_VariantId(currentUserId, variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        cartItemRepository.delete(item);
        return "Đã xóa khỏi giỏ hàng";
    }

    // ==========================================
    // 3. API ĐỔI BIẾN THỂ TRỰC TIẾP TRONG GIỎ 
    // ==========================================
    @Transactional
    public String changeVariant(ChangeVariantRequest request) {
        Long currentUserId = getCurrentUserId();

        // 1. Tìm item cũ đang nằm trong giỏ
        CartItem currentItem = cartItemRepository.findByCartItemIdAndUser_UserId(request.getCartItemId(), currentUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm này trong giỏ hàng"));

        // 2. Tìm biến thể mới mà user vừa chọn
        ProductVariant newVariant = variantRepository.findById(request.getNewVariantId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phân loại mới"));

        // 3. Bảo mật: Đảm bảo biến thể mới vẫn thuộc CÙNG MỘT SẢN PHẨM (Tránh hacker đổi ID sang đồ đắt tiền)
        if (!currentItem.getVariant().getProduct().getProductId().equals(newVariant.getProduct().getProductId())) {
            throw new RuntimeException("Không thể đổi sang phân loại của một sản phẩm khác!");
        }

        // 4. Kiểm tra xem giỏ hàng đã có sẵn biến thể mới này chưa
        Optional<CartItem> existingNewVariantItem = cartItemRepository.findByUser_UserIdAndVariant_VariantId(currentUserId, request.getNewVariantId());

        if (existingNewVariantItem.isPresent()) {
            // Trường hợp ĐÃ CÓ: Cộng dồn số lượng vào dòng đã có và xóa dòng cũ đi
            CartItem targetItem = existingNewVariantItem.get();
            int totalQuantity = targetItem.getQuantity() + currentItem.getQuantity();
            
            if (totalQuantity > newVariant.getStockQuantity()) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho hiện tại");
            }
            
            targetItem.setQuantity(totalQuantity);
            cartItemRepository.save(targetItem);
            cartItemRepository.delete(currentItem); // Xóa dòng cũ
            return "Đã gộp số lượng vào phân loại có sẵn trong giỏ.";
        } else {
            // Trường hợp CHƯA CÓ: Cập nhật trực tiếp biến thể của dòng hiện tại
            if (currentItem.getQuantity() > newVariant.getStockQuantity()) {
                throw new RuntimeException("Số lượng đang chọn vượt quá tồn kho của phân loại mới");
            }
            currentItem.setVariant(newVariant);
            cartItemRepository.save(currentItem);
            return "Đã thay đổi phân loại thành công.";
        }
    }
}