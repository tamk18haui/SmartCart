# SmartCart API File Structure & Mapping

## Tóm tắt chi tiết cấu trúc file và xử lý API

### 📁 Cấu trúc Thư mục Chính

```
SmartCart/
├── src/main/java/com/gr6/SmartCart/
│   ├── common/                          # Các class/utility chung
│   │   ├── base/
│   │   │   ├── BaseResponse.java        # Format response chung cho tất cả API
│   │   │   └── PageResponse.java        # Format pagination
│   │   ├── domain/                      # Domain models
│   │   ├── enums/                       # Các enum (ProductStatus, UserRole, etc)
│   │   ├── exception/                   # Exception handlers
│   │   └── security/                    # Security config, JWT, Authentication
│   │
│   ├── modules/                         # API v1 modules
│   ├── module_v2/                       # API v2 modules
│   └── module_v3/                       # API v3 modules
│
├── src/main/resources/
│   ├── application.yaml                 # Main configuration
│   └── db/migration/                    # Database migrations (Flyway/Liquibase)
│
└── .mvn/wrapper/                        # Maven wrapper configuration
```

---

## 🔐 Module 1: Identity (Xác thực & Quản lý tài khoản)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/modules/identity/`

### Controllers
| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `LoginController.java` | `/api/v1/auth/login` | POST | Đăng nhập người dùng |
| `RegistrationController.java` | `/api/v1/auth/register` | POST | Đăng ký người dùng mới |
| `ShopRegistrationController.java` | `/api/v1/shops/register` | POST | Đăng ký cửa hàng |
| `ShopManagementController.java` | `/api/v1/shops/update` | PUT | Cập nhật thông tin cửa hàng |
| `ShopManagementController.java` | `/api/v1/shops/info` | GET | Lấy thông tin cửa hàng của seller |
| `AdminAccountController.java` | `/api/v1/admin/users` | GET | Quản lý người dùng (Admin) |
| `AdminAccountController.java` | `/api/v1/admin/users/{userId}/ban` | PATCH | Cấm người dùng (Admin) |
| `AdminAccountController.java` | `/api/v1/admin/users/{userId}/unban` | PATCH | Bỏ cấm người dùng (Admin) |
| `AdminAccountController.java` | `/api/v1/admin/shops` | GET | Quản lý cửa hàng (Admin) |
| `AdminAccountController.java` | `/api/v1/admin/shops/{shopId}/approve` | PATCH | Duyệt cửa hàng (Admin) |
| `AdminAccountController.java` | `/api/v1/admin/shops/{shopId}/reject` | PATCH | Từ chối cửa hàng (Admin) |
| `AdminAccountController.java` | `/api/v1/admin/shops/{shopId}/ban` | PATCH | Cấm cửa hàng (Admin) |
| `AdminAccountController.java` | `/api/v1/admin/shops/{shopId}/unban` | PATCH | Bỏ cấm cửa hàng (Admin) |

### Services
| Service | Chức năng |
|---------|----------|
| `LoginService.java` | Xác thực user, phát hành JWT token |
| `RegisterService.java` | Tạo tài khoản mới, hash password |
| `ShopRegistrationService.java` | Tạo cửa hàng mới |
| `ShopManagerService.java` | Cập nhật/lấy thông tin cửa hàng |
| `AdminAccountService.java` | Quản lý user/shop cho admin |

### DTOs
```
Request:
- LoginRequest.java (email, password)
- RegisterRequest.java (email, password, fullName, phone)
- ShopRegisterRequest.java (shopName, description, etc)
- ShopManagerRequest.java (shopName, description, avatar, etc)

Response:
- LoginResponse.java
- ShopAdminResponse.java
- UserAdminResponse.java
```

### Entities/Repositories
```
Entity:
- User.java (PK: userId, Columns: email, password, fullName, phone, role, status)
- Shop.java (PK: shopId, FK: userId, Columns: shopName, status, rating)
- Role.java
- UserStatus.java

Repository:
- UserRepository.java
- ShopRepository.java
- RoleRepository.java
```

### Database Tables
- `users` - Thông tin người dùng
- `shops` - Thông tin cửa hàng
- `roles` - Vai trò (BUYER, SELLER, ADMIN)
- `user_roles` - Mapping user với roles

---

## 🛒 Module 2: Catalog (Quản lý sản phẩm & Danh mục)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/modules/catalog/`

### Controllers
| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `CategoryController.java` | `/api/v1/categories` | GET | Lấy tất cả danh mục |
| `CategoryController.java` | `/api/v1/categories` | POST | Tạo danh mục (Admin) |
| `CategoryController.java` | `/api/v1/categories/{id}` | PUT | Cập nhật danh mục (Admin) |
| `CategoryController.java` | `/api/v1/categories/{id}/toggle-status` | PATCH | Ẩn/Hiện danh mục (Admin) |
| `ProductController.java` | `/api/v1/products` | POST | Tạo sản phẩm mới |
| `ProductController.java` | `/api/v1/products/{productId}` | PUT | Cập nhật sản phẩm |
| `ProductController.java` | `/api/v1/products/{productId}` | DELETE | Xóa sản phẩm |
| `ProductController.java` | `/api/v1/products/shop/{shopId}` | GET | Lấy sản phẩm theo cửa hàng (Pagination) |
| `ProductController.java` | `/api/v1/products/seller/{productId}` | GET | Lấy chi tiết sản phẩm cho seller |
| `AdminProductController.java` | `/api/v1/admin/products` | GET | Quản lý sản phẩm (Admin) |
| `AdminProductController.java` | `/api/v1/admin/products/{productId}/ban` | PATCH | Cấm sản phẩm (Admin) |
| `AdminProductController.java` | `/api/v1/admin/products/{productId}/unban` | PATCH | Bỏ cấm sản phẩm (Admin) |
| `AdminProductController.java` | `/api/v1/admin/products/{productId}` | DELETE | Xóa sản phẩm (Admin) |
| `ProductVariantController.java` | `/api/v1/variants` | POST | Tạo variant sản phẩm |
| `ProductVariantController.java` | `/api/v1/variants/{variantId}` | PUT | Cập nhật variant |
| `ProductVariantController.java` | `/api/v1/variants/{variantId}` | DELETE | Xóa variant |
| `InventoryController.java` | `/api/v1/inventory/decrease` | POST | Giảm tồn kho |
| `InventoryController.java` | `/api/v1/inventory/increase` | POST | Tăng tồn kho |

### Services
| Service | Chức năng |
|---------|----------|
| `CategoryService.java` | CRUD danh mục, lấy tất cả danh mục |
| `ProductService.java` | CRUD sản phẩm, filter theo shop |
| `AdminProductService.java` | Quản lý sản phẩm cho admin (ban, unban, delete) |
| `ProductVariantService.java` | CRUD variant sản phẩm |
| `InventoryService.java` | Quản lý tồn kho, giảm/tăng stock |

### DTOs
```
Request:
- CategoryRequest.java (name, description, image)
- ProductRequest.java (name, description, categoryId, price, images, tags)
- VariantCreateRequest.java (productId, color, size, price, stock, sku)
- InventoryUpdateRequest.java (variantId, quantity)

Response:
- CategoryResponse.java
- ProductResponse.java
- VariantResponse.java
```

### Entities/Repositories
```
Entity:
- Category.java (PK: id, Columns: name, description, isActive)
- Product.java (PK: id, FK: shopId, categoryId, Columns: name, price, status)
- ProductVariant.java (PK: id, FK: productId, Columns: color, size, price, stock, sku)

Repository:
- CategoryRepository.java
- ProductRepository.java
- ProductVariantRepository.java
```

### Database Tables
- `categories` - Danh mục sản phẩm
- `products` - Sản phẩm
- `product_variants` - Biến thể sản phẩm (màu, size, etc)
- `product_images` - Ảnh sản phẩm

---

## 🛍️ Module 3: Storefront (Cửa hàng công khai, Giỏ hàng)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/modules/storefront/`

### Controllers
| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `ShopPublicController.java` | `/api/v1/storefront/shops/{shopId}` | GET | Lấy thông tin cửa hàng công khai |
| `ShopPublicController.java` | `/api/v1/storefront/shops/{shopId}/products` | GET | Lấy sản phẩm theo cửa hàng (Pagination) |
| `DiscoveryController.java` | `/api/v1/storefront/discovery/home-products` | GET | Lấy sản phẩm trang chủ |
| `DiscoveryController.java` | `/api/v1/storefront/discovery/search` | POST | Tìm kiếm & lọc sản phẩm (Pagination) |
| `CartController.java` | `/api/v1/storefront/cart/items` | GET | Lấy danh sách giỏ hàng |
| `CartController.java` | `/api/v1/storefront/cart/add` | POST | Thêm sản phẩm vào giỏ |
| `CartController.java` | `/api/v1/storefront/cart/update` | PUT | Cập nhật số lượng trong giỏ |
| `CartController.java` | `/api/v1/storefront/cart/change-variant` | PUT | Thay đổi variant sản phẩm trong giỏ |
| `CartController.java` | `/api/v1/storefront/cart/remove/{variantId}` | DELETE | Xóa sản phẩm khỏi giỏ |

### Services
| Service | Chức năng |
|---------|----------|
| `ShopPublicService.java` | Lấy thông tin cửa hàng công khai, sản phẩm |
| `DiscoveryService.java` | Tìm kiếm/lọc sản phẩm, lấy sản phẩm trang chủ |
| `CartService.java` | Quản lý giỏ hàng (add, update, remove) |

### DTOs
```
Request:
- CartItemRequest.java (variantId, quantity, shopId)
- ChangeVariantRequest.java (oldVariantId, newVariantId, quantity)
- SearchFilterRequest.java (keyword, categoryId, minPrice, maxPrice, rating)

Response:
- CartDetailResponseDTO.java
- ShopPublicResponse.java
- ShopProductResponse.java
- ProductResponseDTO.java
```

### Entities/Repositories
```
Entity:
- Cart.java (PK: id, FK: userId)
- CartItem.java (PK: id, FK: cartId, variantId)

Repository:
- CartRepository.java
- CartItemRepository.java
```

### Database Tables
- `carts` - Giỏ hàng
- `cart_items` - Mục trong giỏ hàng

---

## 💳 Module 4: Finance Core (Đơn hàng & Thanh toán)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/modules/finance_core/`

### Controllers
| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `OrderController.java` | `/api/v1/orders/preview` | POST | Lấy preview checkout |
| `OrderController.java` | `/api/v1/orders/checkout` | POST | Tạo đơn hàng mới |
| `OrderController.java` | `/api/v1/orders/payment/callback` | POST | Xử lý callback thanh toán |
| `BuyerVoucherController.java` | `/api/v1/vouchers/shop/{shopId}` | GET | Lấy voucher của cửa hàng |
| `PaymentReturnController.java` | `/api/v1/payments/momo/return` | GET | MoMo return URL |
| `PaymentReturnController.java` | `/api/v1/payments/momo/ipn` | POST | MoMo IPN notification |
| `PaymentReturnController.java` | `/api/v1/payments/vnpay/return` | GET | VNPay return URL |
| `PaymentReturnController.java` | `/api/v1/payments/vnpay/ipn` | GET | VNPay IPN notification |
| `DevPaymentController.java` | `/api/v1/payments/dev/{provider}/success` | GET | Dev payment success (test) |
| `DevPaymentController.java` | `/api/v1/payments/dev/{provider}/fail` | GET | Dev payment fail (test) |

### Services
| Service | Chức năng |
|---------|----------|
| `OrderService.java` | Tạo đơn hàng, xử lý callback thanh toán, lấy preview |
| `VoucherService.java` | Lấy voucher cho buyer |
| `PaymentGatewayService.java` | Tích hợp cổng thanh toán (MoMo, VNPay) |

### DTOs
```
Request:
- CheckoutPreviewRequest.java (cartItems, addressId, shippingMethod)
- CreateOrderRequest.java (cartItems, addressId, paymentMethod, voucherId)
- PaymentCallbackRequest.java (orderId, transactionId, paymentProvider, success)

Response:
- CheckoutPreviewResponse.java (subtotal, tax, discount, total)
- PaymentCallbackRequest.java (orderId, status)
```

### Entities/Repositories
```
Entity:
- Order.java (PK: id, FK: userId, Columns: totalAmount, status, createdAt)
- ShopOrder.java (PK: id, FK: orderId, shopId, Columns: status)
- OrderItem.java (PK: id, FK: shopOrderId, variantId, Columns: quantity, price)
- Payment.java (PK: id, FK: orderId, Columns: method, status, amount)

Repository:
- OrderRepository.java
- ShopOrderRepository.java
- OrderItemRepository.java
- PaymentRepository.java
```

### Database Tables
- `orders` - Đơn hàng chính
- `shop_orders` - Đơn hàng theo từng cửa hàng
- `order_items` - Mục trong đơn hàng
- `payments` - Thông tin thanh toán

### Utilities
- `PaymentCryptoUtil.java` - Mã hóa/xác minh chữ ký thanh toán

---

## 💬 Module 5: Chat (Realtime Messaging)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/modules/chat/`

### Controllers
| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `ChatRestController.java` | `/api/v1/chat/conversations` | GET | Lấy danh sách hội thoại |
| `ChatRestController.java` | `/api/v1/chat/messages/{partnerId}` | GET | Lấy tin nhắn với một người (Pagination) |
| `ChatRestController.java` | `/api/v1/chat/messages/{partnerId}/read` | PATCH | Đánh dấu tin nhắn đã đọc |
| `ChatWebSocketController.java` | `/app/chat.send` | WEBSOCKET | Gửi tin nhắn realtime |

### Services
| Service | Chức năng |
|---------|----------|
| `ChatService.java` | Lưu/lấy tin nhắn, quản lý hội thoại |

### DTOs
```
Request:
- ChatMessageRequest.java (receiverId, message, conversationId)

Response:
- ChatMessageResponse.java (id, senderId, receiverId, message, timestamp)
- ConversationResponse.java (conversationId, partnerId, lastMessage, unreadCount)
```

### Entities/Repositories
```
Entity:
- ChatMessage.java (PK: id, FK: conversationId, senderId, receiverId)
- Conversation.java (PK: id, FK: userId1, userId2, Columns: lastMessageAt)

Repository:
- ChatMessageRepository.java
- ConversationRepository.java
```

### Database Tables
- `chat_messages` - Tin nhắn
- `conversations` - Hội thoại giữa 2 người

### Configuration
- `WebSocketConfig.java` - Cấu hình WebSocket endpoint
- `StompConfig.java` - STOMP (Simple Text Oriented Messaging Protocol)

---

## 📦 Module 6: Fulfillment (Xử lý đơn hàng)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/modules/fulfillment/`

### Controllers
| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `OrderQueryController.java` | `/api/v1/shop-orders` | GET | Lấy danh sách đơn hàng (Seller) |
| `OrderQueryController.java` | `/api/v1/shop-orders/{id}` | GET | Lấy chi tiết đơn hàng (Seller) |
| `OrderActionController.java` | `/api/v1/shop-orders/{id}/confirm` | PUT | Xác nhận/chuẩn bị hàng (Seller) |
| `OrderActionController.java` | `/api/v1/shop-orders/{shopOrderId}/status` | PUT | Cập nhật trạng thái đơn hàng (Seller) |
| `OrderActionController.java` | `/api/v1/shop-orders/{id}/` | PUT | Hủy đơn hàng (Seller) |

### Services
| Service | Chức năng |
|---------|----------|
| `OrderQueryService.java` | Lấy danh sách/chi tiết đơn hàng cho seller |
| `OrderActionService.java` | Cập nhật trạng thái, xác nhận, hủy đơn hàng |

### DTOs
```
Request:
- UpdateShopOrderStatusRequest.java (status, trackingNumber)
- CancelOrderRequest.java (reason, refundAmount)

Response:
- OrderListResponse.java (id, orderId, customerName, itemCount, totalAmount)
- OrderDetailResponse.java (id, orderId, items, shippingAddress, totalAmount)
```

### Entities/Repositories
```
Repository:
- ShopOrderRepository.java
- OrderItemRepository.java
```

### Database Tables
- `shop_orders` - Đơn hàng theo cửa hàng
- `order_items` - Mục trong đơn hàng

---

## 💰 Module 7: Vouchers & Promotions

### v1 (Buyer)
**File:** `BuyerVoucherController.java` → `/api/v1/vouchers/shop/{shopId}`
- Lấy voucher có sẵn từ cửa hàng

### v2 (Seller)
**Đường dẫn:** `src/main/java/com/gr6/SmartCart/module_v2/promotion/`

| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `VoucherV2Controller.java` | `/api/v2/seller/vouchers` | GET | Lấy danh sách voucher của seller |
| `VoucherV2Controller.java` | `/api/v2/seller/vouchers` | POST | Tạo voucher mới |
| `VoucherV2Controller.java` | `/api/v2/seller/vouchers/{voucherId}` | PUT | Cập nhật voucher |
| `VoucherV2Controller.java` | `/api/v2/seller/vouchers/{voucherId}` | DELETE | Vô hiệu hóa voucher |

### Services
| Service | Chức năng |
|---------|----------|
| `VoucherService.java` (v1) | Lấy voucher cho buyer |
| `VoucherV2Service.java` (v2) | CRUD voucher cho seller |

### DTOs
```
Request:
- VoucherRequest.java (code, discountType, discountValue, minOrderValue)

Response:
- VoucherResponse.java
- ShopVoucherResponse.java
```

### Database Tables
- `vouchers` - Voucher/Coupon

---

## 👤 Module 8: User Profile & Address (v2)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/module_v2/user/`

### Controllers
| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `ProfileController.java` | `/api/v2/user/profile` | GET | Lấy thông tin profile |
| `ProfileController.java` | `/api/v2/user/profile` | PUT | Cập nhật profile |
| `AddressController.java` | `/api/v2/customer/addresses` | GET | Lấy danh sách địa chỉ |
| `AddressController.java` | `/api/v2/customer/addresses` | POST | Tạo địa chỉ mới |
| `AddressController.java` | `/api/v2/customer/addresses/{id}` | PUT | Cập nhật địa chỉ |
| `AddressController.java` | `/api/v2/customer/addresses/{id}` | DELETE | Xóa địa chỉ |
| `AddressController.java` | `/api/v2/customer/addresses/{id}/set-default` | PUT | Đặt làm địa chỉ mặc định |

### Services
| Service | Chức năng |
|---------|----------|
| `ProfileService.java` | Lấy/cập nhật profile người dùng |
| `AddressService.java` | CRUD địa chỉ giao hàng |

### DTOs
```
Request:
- ProfileDTO.java (fullName, phone, avatar)
- AddressRequestDTO.java (fullName, phone, street, ward, district, province)

Response:
- ProfileDTO.java
- AddressResponseDTO.java
```

### Database Tables
- `addresses` - Địa chỉ giao hàng

---

## 🔐 Module 9: Password Management (v2)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/module_v2/auth/`

| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `PasswordController.java` | `/api/v2/auth/forgot-password` | POST | Gửi OTP quên mật khẩu |
| `PasswordController.java` | `/api/v2/auth/reset-password` | POST | Reset mật khẩu |

**Services:** `PasswordResetService.java`

---

## 📦 Module 10: Order Tracking & Refund (v2)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/module_v2/order_v2/`

### Controllers
| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `TrackingController.java` | `/api/v2/orders/tracking/history` | GET | Lấy lịch sử đơn hàng |
| `TrackingController.java` | `/api/v2/orders/tracking/{shopOrderId}` | GET | Lấy thông tin tracking đơn hàng |
| `RefundController.java` | `/api/v2/orders/refund/{shopOrderId}/cancel` | POST | Yêu cầu hoàn hàng/refund |

### Services
| Service | Chức năng |
|---------|----------|
| `TrackingService.java` | Lấy lịch sử & tracking đơn hàng |
| `RefundService.java` | Xử lý yêu cầu hoàn hàng |

### DTOs
```
Response:
- OrderHistoryResponse.java
- OrderTrackingResponse.java
- RefundRequest.java
```

### Database Tables
- `refunds` - Yêu cầu hoàn hàng

---

## 💵 Module 11: Seller Withdraw & Admin Settlement (v3)

**Đường dẫn:** `src/main/java/com/gr6/SmartCart/module_v3/withdraw/`

### Controllers
| File | Endpoint | HTTP Method | Chức năng |
|------|----------|-------------|----------|
| `SellerWithdrawController.java` | `/api/v3/seller/withdraw/wallet` | GET | Lấy thông tin ví |
| `SellerWithdrawController.java` | `/api/v3/seller/withdraw/wallet/transactions` | GET | Lấy giao dịch ví (Pagination) |
| `SellerWithdrawController.java` | `/api/v3/seller/withdraw/requests` | POST | Tạo yêu cầu rút tiền |
| `SellerWithdrawController.java` | `/api/v3/seller/withdraw/requests` | GET | Lấy danh sách yêu cầu rút tiền |
| `SellerWithdrawController.java` | `/api/v3/seller/withdraw/settlements` | GET | Lấy danh sách cấu trúc thanh toán |
| `AdminWithdrawController.java` | `/api/v3/admin/withdraw/reconcile` | POST | Cân bằng đơn hàng hoàn thành (Admin) |
| `AdminWithdrawController.java` | `/api/v3/admin/withdraw/settlements` | GET | Lấy danh sách cấu trúc thanh toán (Admin) |
| `AdminWithdrawController.java` | `/api/v3/admin/withdraw/requests` | GET | Lấy danh sách yêu cầu rút tiền (Admin) |
| `AdminWithdrawController.java` | `/api/v3/admin/withdraw/requests/{withdrawId}/approve` | PATCH | Duyệt yêu cầu rút tiền (Admin) |
| `AdminWithdrawController.java` | `/api/v3/admin/withdraw/requests/{withdrawId}/reject` | PATCH | Từ chối yêu cầu rút tiền (Admin) |

### Services
| Service | Chức năng |
|---------|----------|
| `SellerWithdrawService.java` | Quản lý ví seller, yêu cầu rút tiền |
| `AdminWithdrawService.java` | Quản lý yêu cầu rút tiền, cấu trúc thanh toán (Admin) |

### DTOs
```
Request:
- WithdrawCreateRequest.java (amount, bankAccountId)
- AdminWithdrawDecisionRequest.java (note, reason)

Response:
- WalletSummaryResponse.java
- WalletTransactionResponse.java
- WithdrawResponse.java
- SellerSettlementResponse.java
- ReconcileResponse.java
```

### Database Tables
- `wallets` - Ví của seller
- `wallet_transactions` - Giao dịch ví
- `withdrawals` - Yêu cầu rút tiền
- `settlements` - Cấu trúc thanh toán

---

## 📊 File Configuration Chính

### 1. Application Configuration
```
src/main/resources/application.yaml
- Database configuration (MySQL)
- JWT configuration
- Payment gateway configuration
- Mail server configuration
- File upload configuration
- CORS configuration
```

### 2. Security Configuration
```
src/main/java/com/gr6/SmartCart/common/security/
- JwtTokenProvider.java - Tạo/xác minh JWT token
- JwtAuthenticationFilter.java - Filter xác thực JWT
- SecurityConfig.java - Spring Security configuration
```

### 3. Exception Handling
```
src/main/java/com/gr6/SmartCart/common/exception/
- GlobalExceptionHandler.java - Handler cho tất cả exceptions
- CustomException.java - Custom exceptions
```

### 4. Base Classes
```
src/main/java/com/gr6/SmartCart/common/base/
- BaseResponse.java - Định dạng response chung
- PageResponse.java - Định dạng pagination
- BaseEntity.java - Base entity với audit info
```

---

## 🔄 API Request/Response Flow

### Ví dụ: Create Order Flow

```
1. Frontend gọi: POST /api/v1/orders/checkout
   ↓
2. OrderController.createOrder() nhận request
   ↓
3. OrderService.createOrder() xử lý
   - Validate input
   - Lấy user info từ SecurityContext
   - Tạo Order entity
   - Tạo ShopOrder entities (1 cho mỗi shop)
   - Tạo OrderItem entities
   - Giảm inventory cho mỗi variant
   - Lưu vào database
   ↓
4. OrderService gọi PaymentGatewayService
   - Tạo payment request
   - Gọi external payment API (MoMo/VNPay)
   - Nhận payment URL
   ↓
5. OrderController trả về response
   {
     "code": 201,
     "message": "Order created",
     "data": {
       "orderId": 1,
       "paymentUrl": "https://..."
     }
   }
   ↓
6. Frontend redirect user đến payment URL
   ↓
7. User hoàn thành thanh toán
   ↓
8. Payment gateway gọi callback:
   POST /api/v1/orders/payment/callback
   ↓
9. OrderController.handlePaymentCallback() 
   - Xác minh signature
   - Cập nhật Order status thành PAID
   - Trigger email notification
```

---

## 📝 Quy ước Coding

### Naming Convention
- **Controllers:** `*Controller.java` (e.g., `ProductController.java`)
- **Services:** `*Service.java` (e.g., `ProductService.java`)
- **DTOs:** `*Request.java`, `*Response.java`, `*DTO.java`
- **Repositories:** `*Repository.java` (extends `JpaRepository`)
- **Entities:** Entity names (e.g., `Product.java`, `Order.java`)
- **Enums:** `*Status.java`, `*Type.java`, `*Role.java`

### Package Structure
```
module/
├── controller/    # REST controllers
├── service/       # Business logic
├── dto/          # Data transfer objects
├── entity/       # JPA entities
├── repository/   # Data access layer
└── util/         # Utility classes
```

---

## 🗂️ Complete File Listing

```
src/main/java/com/gr6/SmartCart/

modules/
├── catalog/
│   ├── controller/
│   │   ├── AdminProductController.java
│   │   ├── CategoryController.java
│   │   ├── InventoryController.java
│   │   ├── ProductController.java
│   │   └── ProductVariantController.java
│   ├── service/
│   │   ├── AdminProductService.java
│   │   ├── CategoryService.java
│   │   ├── InventoryService.java
│   │   ├── ProductService.java
│   │   └── ProductVariantService.java
│   ├── dto/
│   ├── entity/
│   └── repository/
│
├── storefront/
│   ├── controller/
│   │   ├── CartController.java
│   │   ├── DiscoveryController.java
│   │   └── ShopPublicController.java
│   ├── service/
│   ├── dto/
│   ├── entity/
│   └── repository/
│
├── finance_core/
│   ├── controller/
│   │   ├── BuyerVoucherController.java
│   │   ├── DevPaymentController.java
│   │   ├── OrderController.java
│   │   ├── PaymentReturnController.java
│   │   └── PaymentCallbackHandler.java
│   ├── service/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   └── util/
│       └── PaymentCryptoUtil.java
│
├── chat/
│   ├── controller/
│   │   ├── ChatRestController.java
│   │   └── ChatWebSocketController.java
│   ├── service/
│   ├── dto/
│   ├── config/
│   │   ├── WebSocketConfig.java
│   │   └── StompConfig.java
│   ├── entity/
│   └── repository/
│
├── fulfillment/
│   ├── controller/
│   │   ├── OrderActionController.java
│   │   └── OrderQueryController.java
│   ├── service/
│   ├── dto/
│   └── repository/
│
└── identity/
    ├── controller/
    │   ├── AdminAccountController.java
    │   ├── LoginController.java
    │   ├── RegistrationController.java
    │   ├── ShopManagementController.java
    │   └── ShopRegistrationController.java
    ├── service/
    ├── dto/
    ├── entity/
    └── repository/

module_v2/
├── auth/
│   └── controller/
│       └── PasswordController.java
├── order_v2/
│   ├── controller/
│   │   ├── RefundController.java
│   │   └── TrackingController.java
│   └── service/
├── promotion/
│   └── controller/
│       └── VoucherV2Controller.java
└── user/
    ├── controller/
    │   ├── AddressController.java
    │   └── ProfileController.java
    └── service/

module_v3/
├── analytics/
│   └── (currently empty)
└── withdraw/
    ├── controller/
    │   ├── AdminWithdrawController.java
    │   └── SellerWithdrawController.java
    └── service/

common/
├── base/
│   ├── BaseResponse.java
│   ├── PageResponse.java
│   └── BaseEntity.java
├── domain/
│   ├── Address.java
│   ├── CartItem.java
│   ├── Category.java
│   ├── Order.java
│   ├── Product.java
│   └── ...
├── enums/
│   ├── OrderStatus.java
│   ├── PaymentProvider.java
│   ├── ProductStatus.java
│   ├── ShopStatus.java
│   ├── UserRole.java
│   ├── UserStatus.java
│   ├── WithdrawStatus.java
│   └── ...
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── CustomException.java
│   └── ...
└── security/
    ├── JwtTokenProvider.java
    ├── JwtAuthenticationFilter.java
    └── SecurityConfig.java
```

Tài liệu này cung cấp mapping chi tiết về:
✅ Tất cả endpoints API
✅ Controllers xử lý mỗi endpoint
✅ Services thực hiện logic
✅ DTOs cho request/response
✅ Database entities & tables
✅ Flow xử lý request
✅ Cấu trúc file đầy đủ
