# SmartCart API Documentation

## Mục lục
1. [Authentication APIs](#authentication-apis)
2. [User & Profile APIs](#user--profile-apis)
3. [Product & Catalog APIs](#product--catalog-apis)
4. [Cart APIs](#cart-apis)
5. [Order APIs](#order-apis)
6. [Payment APIs](#payment-apis)
7. [Shop APIs](#shop-apis)
8. [Voucher APIs](#voucher-apis)
9. [Chat APIs](#chat-apis)
10. [Fulfillment APIs](#fulfillment-apis)
11. [Withdraw & Settlement APIs](#withdraw--settlement-apis)
12. [Admin APIs](#admin-apis)

---

## Authentication APIs

### 1. Login
**Controller:** `LoginController.java` - `/api/v1/auth`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/identity/controller/LoginController.java`

```
POST /api/v1/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "user@example.com",
    "role": "BUYER"
  }
}
```

**Service:** `LoginService.java`
**DTO:** `LoginRequest`, `LoginResponse`

---

### 2. Register User
**Controller:** `RegistrationController.java` - `/api/v1/auth`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/identity/controller/RegistrationController.java`

```
POST /api/v1/auth/register
```

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "password": "password123",
  "fullName": "User Full Name",
  "phone": "0987654321"
}
```

**Response:**
```json
{
  "code": 201,
  "message": "User registered successfully",
  "data": null
}
```

**Service:** `RegisterService.java`
**DTO:** `RegisterRequest`
**Database Tables:** `users`

---

### 3. Forgot Password
**Controller:** `PasswordController.java` - `/api/v2/auth`
**File Path:** `src/main/java/com/gr6/SmartCart/module_v2/auth/controller/PasswordController.java`

```
POST /api/v2/auth/forgot-password?email=user@example.com
```

**Response:**
```json
{
  "code": 200,
  "message": "OTP sent to your email",
  "data": null
}
```

**Service:** `PasswordResetService.java`

---

### 4. Reset Password
**Controller:** `PasswordController.java` - `/api/v2/auth`

```
POST /api/v2/auth/reset-password
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Password reset successfully",
  "data": null
}
```

**Service:** `PasswordResetService.java`
**DTO:** `ResetPasswordRequest`

---

### 5. Register Shop
**Controller:** `ShopRegistrationController.java` - `/api/v1/shops`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/identity/controller/ShopRegistrationController.java`

```
POST /api/v1/shops/register
```

**Request Body:**
```json
{
  "userId": 1,
  "shopName": "My Shop",
  "description": "Shop description",
  "avatar": "https://...",
  "email": "shop@example.com"
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Shop registered successfully",
  "data": null
}
```

**Service:** `ShopRegistrationService.java`
**DTO:** `ShopRegisterRequest`
**Database Tables:** `shops`

---

## User & Profile APIs

### 1. Get User Profile
**Controller:** `ProfileController.java` - `/api/v2/user/profile`
**File Path:** `src/main/java/com/gr6/SmartCart/module_v2/user/controller/ProfileController.java`

```
GET /api/v2/user/profile
```

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get profile successfully",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "fullName": "User Name",
    "phone": "0987654321",
    "avatar": "https://...",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

**Service:** `ProfileService.java`
**DTO:** `ProfileDTO`

---

### 2. Update User Profile
**Controller:** `ProfileController.java`

```
PUT /api/v2/user/profile
```

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "fullName": "New Name",
  "phone": "0123456789",
  "avatar": "https://..."
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Profile updated successfully",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "fullName": "New Name",
    "phone": "0123456789",
    "avatar": "https://..."
  }
}
```

**Service:** `ProfileService.java`
**DTO:** `ProfileDTO`

---

### 3. Get User Addresses
**Controller:** `AddressController.java` - `/api/v2/customer/addresses`
**File Path:** `src/main/java/com/gr6/SmartCart/module_v2/user/controller/AddressController.java`

```
GET /api/v2/customer/addresses
```

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get address list successfully",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "fullName": "User Name",
      "phone": "0987654321",
      "street": "123 Main St",
      "ward": "Ward 1",
      "district": "District 1",
      "province": "City",
      "isDefault": true
    }
  ]
}
```

**Service:** `AddressService.java`
**DTO:** `AddressResponseDTO`
**Database Tables:** `addresses`

---

### 4. Create New Address
**Controller:** `AddressController.java`

```
POST /api/v2/customer/addresses
```

**Request Body:**
```json
{
  "fullName": "User Name",
  "phone": "0987654321",
  "street": "123 Main St",
  "ward": "Ward 1",
  "district": "District 1",
  "province": "City",
  "isDefault": false
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Address created successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "fullName": "User Name",
    "phone": "0987654321",
    "street": "123 Main St",
    "isDefault": false
  }
}
```

**DTO:** `AddressRequestDTO`, `AddressResponseDTO`

---

### 5. Update Address
**Controller:** `AddressController.java`

```
PUT /api/v2/customer/addresses/{id}
```

**Request Body:**
```json
{
  "fullName": "New Name",
  "phone": "0123456789",
  "street": "456 New St",
  "ward": "Ward 2",
  "district": "District 2",
  "province": "City"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Address updated successfully",
  "data": {
    "id": 1,
    "fullName": "New Name",
    "phone": "0123456789"
  }
}
```

---

### 6. Delete Address
**Controller:** `AddressController.java`

```
DELETE /api/v2/customer/addresses/{id}
```

**Response:**
```json
{
  "code": 200,
  "message": "Address deleted successfully",
  "data": null
}
```

---

### 7. Set Default Address
**Controller:** `AddressController.java`

```
PUT /api/v2/customer/addresses/{id}/set-default
```

**Response:**
```json
{
  "code": 200,
  "message": "Default address set successfully",
  "data": null
}
```

**Service:** `AddressService.java`

---

## Product & Catalog APIs

### 1. Get All Categories
**Controller:** `CategoryController.java` - `/api/v1/categories`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/catalog/controller/CategoryController.java`

```
GET /api/v1/categories
```

**Response:**
```json
{
  "code": 200,
  "message": "Get categories successfully",
  "data": [
    {
      "id": 1,
      "name": "Electronics",
      "description": "Electronic devices",
      "image": "https://...",
      "isActive": true
    }
  ]
}
```

**Service:** `CategoryService.java`
**DTO:** `CategoryResponse`
**Database Tables:** `categories`

---

### 2. Create Category (Admin)
**Controller:** `CategoryController.java`

```
POST /api/v1/categories
```

**Headers:**
```
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
  "name": "New Category",
  "description": "Category description",
  "image": "https://..."
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Category created successfully",
  "data": {
    "id": 1,
    "name": "New Category",
    "description": "Category description"
  }
}
```

**DTO:** `CategoryRequest`, `CategoryResponse`

---

### 3. Update Category
**Controller:** `CategoryController.java`

```
PUT /api/v1/categories/{id}
```

**Request Body:**
```json
{
  "name": "Updated Category",
  "description": "Updated description"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Category updated successfully",
  "data": {
    "id": 1,
    "name": "Updated Category"
  }
}
```

---

### 4. Toggle Category Status
**Controller:** `CategoryController.java`

```
PATCH /api/v1/categories/{id}/toggle-status
```

**Response:**
```json
{
  "code": 200,
  "message": "Category status toggled",
  "data": null
}
```

---

### 5. Create Product
**Controller:** `ProductController.java` - `/api/v1/products`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/catalog/controller/ProductController.java`

```
POST /api/v1/products
```

**Headers:**
```
Authorization: Bearer {seller_token}
```

**Request Body:**
```json
{
  "name": "Product Name",
  "description": "Product description",
  "categoryId": 1,
  "shopId": 1,
  "price": 99.99,
  "images": ["https://..."],
  "tags": ["tag1", "tag2"]
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "Product Name",
    "price": 99.99,
    "shopId": 1,
    "status": "ACTIVE"
  }
}
```

**Service:** `ProductService.java`
**DTO:** `ProductRequest`, `ProductResponse`
**Database Tables:** `products`

---

### 6. Get Products by Shop
**Controller:** `ProductController.java`

```
GET /api/v1/products/shop/{shopId}?page=1&size=10
```

**Response:**
```json
{
  "code": 200,
  "message": "Get products successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Product Name",
        "price": 99.99,
        "image": "https://..."
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 1
  }
}
```

---

### 7. Update Product
**Controller:** `ProductController.java`

```
PUT /api/v1/products/{productId}
```

**Request Body:**
```json
{
  "name": "Updated Name",
  "description": "Updated description",
  "price": 119.99
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Product updated successfully",
  "data": {
    "id": 1,
    "name": "Updated Name",
    "price": 119.99
  }
}
```

---

### 8. Delete Product
**Controller:** `ProductController.java`

```
DELETE /api/v1/products/{productId}
```

**Response:**
```json
{
  "code": 200,
  "message": "Product deleted successfully",
  "data": null
}
```

---

### 9. Get Product for Seller
**Controller:** `ProductController.java`

```
GET /api/v1/products/seller/{productId}
```

**Headers:**
```
Authorization: Bearer {seller_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get product successfully",
  "data": {
    "id": 1,
    "name": "Product Name",
    "description": "Description",
    "price": 99.99,
    "stock": 100,
    "sold": 10
  }
}
```

---

### 10. Create Product Variant
**Controller:** `ProductVariantController.java` - `/api/v1/variants`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/catalog/controller/ProductVariantController.java`

```
POST /api/v1/variants
```

**Request Body:**
```json
{
  "productId": 1,
  "color": "Red",
  "size": "M",
  "price": 99.99,
  "stock": 100,
  "sku": "PROD-RED-M",
  "image": "https://..."
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Variant created successfully",
  "data": {
    "id": 1,
    "productId": 1,
    "color": "Red",
    "size": "M",
    "price": 99.99,
    "stock": 100
  }
}
```

**Service:** `ProductVariantService.java`
**DTO:** `VariantCreateRequest`, `VariantResponse`
**Database Tables:** `product_variants`

---

### 11. Update Product Variant
**Controller:** `ProductVariantController.java`

```
PUT /api/v1/variants/{variantId}
```

**Request Body:**
```json
{
  "color": "Blue",
  "size": "L",
  "price": 109.99,
  "stock": 120
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Variant updated successfully",
  "data": {
    "id": 1,
    "color": "Blue",
    "size": "L",
    "price": 109.99
  }
}
```

---

### 12. Delete Product Variant
**Controller:** `ProductVariantController.java`

```
DELETE /api/v1/variants/{variantId}
```

**Response:**
```json
{
  "code": 200,
  "message": "Variant deleted successfully",
  "data": null
}
```

---

### 13. Decrease Stock
**Controller:** `InventoryController.java` - `/api/v1/inventory`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/catalog/controller/InventoryController.java`

```
POST /api/v1/inventory/decrease
```

**Request Body:**
```json
{
  "variantId": 1,
  "quantity": 5
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Stock decreased successfully",
  "data": null
}
```

**Service:** `InventoryService.java`
**DTO:** `InventoryUpdateRequest`

---

### 14. Increase Stock
**Controller:** `InventoryController.java`

```
POST /api/v1/inventory/increase
```

**Request Body:**
```json
{
  "variantId": 1,
  "quantity": 10
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Stock increased successfully",
  "data": null
}
```

---

### 15. Ban Product (Admin)
**Controller:** `AdminProductController.java` - `/api/v1/admin/products`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/catalog/controller/AdminProductController.java`

```
PATCH /api/v1/admin/products/{productId}/ban?reason=Reason
```

**Response:**
```json
{
  "code": 200,
  "message": "Product banned successfully",
  "data": null
}
```

**Service:** `AdminProductService.java`

---

### 16. Unban Product (Admin)
**Controller:** `AdminProductController.java`

```
PATCH /api/v1/admin/products/{productId}/unban
```

**Response:**
```json
{
  "code": 200,
  "message": "Product unbanned successfully",
  "data": null
}
```

---

### 17. Get Admin Products (Admin)
**Controller:** `AdminProductController.java`

```
GET /api/v1/admin/products?keyword=&status=ACTIVE&shopId=&categoryId=&page=1&size=10
```

**Response:**
```json
{
  "code": 200,
  "message": "Get products successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Product",
        "status": "ACTIVE",
        "shopId": 1
      }
    ],
    "totalElements": 100,
    "totalPages": 10
  }
}
```

---

## Cart APIs

### 1. Get Cart Items
**Controller:** `CartController.java` - `/api/v1/storefront/cart`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/storefront/controller/CartController.java`

```
GET /api/v1/storefront/cart/items
```

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get cart items successfully",
  "data": {
    "cartId": 1,
    "items": [
      {
        "variantId": 1,
        "productName": "Product",
        "price": 99.99,
        "quantity": 2,
        "total": 199.98
      }
    ],
    "totalPrice": 199.98,
    "itemCount": 1
  }
}
```

**Service:** `CartService.java`
**DTO:** `CartDetailResponseDTO`
**Database Tables:** `carts`, `cart_items`

---

### 2. Add to Cart
**Controller:** `CartController.java`

```
POST /api/v1/storefront/cart/add
```

**Request Body:**
```json
{
  "variantId": 1,
  "quantity": 2,
  "shopId": 1
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Product added to cart successfully",
  "data": null
}
```

**DTO:** `CartItemRequest`

---

### 3. Update Cart Quantity
**Controller:** `CartController.java`

```
PUT /api/v1/storefront/cart/update
```

**Request Body:**
```json
{
  "variantId": 1,
  "quantity": 5
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Cart quantity updated successfully",
  "data": null
}
```

---

### 4. Change Variant
**Controller:** `CartController.java`

```
PUT /api/v1/storefront/cart/change-variant
```

**Request Body:**
```json
{
  "oldVariantId": 1,
  "newVariantId": 2,
  "quantity": 2
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Variant changed successfully",
  "data": null
}
```

**DTO:** `ChangeVariantRequest`

---

### 5. Remove from Cart
**Controller:** `CartController.java`

```
DELETE /api/v1/storefront/cart/remove/{variantId}
```

**Response:**
```json
{
  "code": 200,
  "message": "Product removed from cart successfully",
  "data": null
}
```

---

## Order APIs

### 1. Get Checkout Preview
**Controller:** `OrderController.java` - `/api/v1/orders`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/finance_core/controller/OrderController.java`

```
POST /api/v1/orders/preview
```

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "cartItems": [
    {
      "variantId": 1,
      "quantity": 2
    }
  ],
  "addressId": 1,
  "shippingMethod": "STANDARD"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get checkout preview successfully",
  "data": {
    "subtotal": 199.98,
    "shippingCost": 10.00,
    "tax": 19.00,
    "discount": 0,
    "total": 228.98,
    "itemCount": 2
  }
}
```

**Service:** `OrderService.java`
**DTO:** `CheckoutPreviewRequest`, `CheckoutPreviewResponse`

---

### 2. Create Order (Checkout)
**Controller:** `OrderController.java`

```
POST /api/v1/orders/checkout
```

**Request Body:**
```json
{
  "cartItems": [
    {
      "variantId": 1,
      "quantity": 2,
      "shopId": 1
    }
  ],
  "addressId": 1,
  "shippingMethod": "STANDARD",
  "paymentMethod": "MOMO",
  "voucherId": null
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Order created successfully",
  "data": {
    "orderId": 1,
    "totalAmount": 228.98,
    "paymentUrl": "https://payment-gateway.com/...",
    "status": "PENDING_PAYMENT"
  }
}
```

**DTO:** `CreateOrderRequest`
**Database Tables:** `orders`, `order_items`, `shop_orders`

---

### 3. Payment Callback
**Controller:** `OrderController.java`

```
POST /api/v1/orders/payment/callback
```

**Request Body:**
```json
{
  "orderId": 1,
  "transactionId": 123456,
  "paymentProvider": "MOMO",
  "providerTransactionId": "MOMO123",
  "success": true,
  "signature": "signature_hash"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Payment processed successfully",
  "data": null
}
```

**DTO:** `PaymentCallbackRequest`

---

## Payment APIs

### 1. MoMo Return URL
**Controller:** `PaymentReturnController.java` - `/api/v1/payments`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/finance_core/controller/PaymentReturnController.java`

```
GET /api/v1/payments/momo/return?orderId=&resultCode=&transId=
```

**Response:**
```html
<html>
  <body>
    <h1>Payment Status: Success/Failed</h1>
    <p>Message: ...</p>
  </body>
</html>
```

**Service:** `OrderService.java`

---

### 2. MoMo IPN Notification
**Controller:** `PaymentReturnController.java`

```
POST /api/v1/payments/momo/ipn
```

**Request Body:**
```json
{
  "orderId": "ORDER_12345",
  "transId": "123456789",
  "resultCode": "0",
  "amount": 228.98,
  "extraData": "base64_encoded_data"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "IPN processed",
  "data": null
}
```

---

### 3. VNPay Return URL
**Controller:** `PaymentReturnController.java`

```
GET /api/v1/payments/vnpay/return?vnp_OrderId=&vnp_ResponseCode=&vnp_SecureHash=
```

**Response:**
```html
<html>
  <body>
    <h1>Payment Status: Success/Failed</h1>
  </body>
</html>
```

---

### 4. VNPay IPN Notification
**Controller:** `PaymentReturnController.java`

```
GET /api/v1/payments/vnpay/ipn?vnp_OrderId=&vnp_ResponseCode=&vnp_SecureHash=
```

**Response:**
```json
{
  "code": 200,
  "message": "IPN processed",
  "data": null
}
```

---

### 5. Dev Payment Success (Testing)
**Controller:** `DevPaymentController.java` - `/api/v1/payments/dev`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/finance_core/controller/DevPaymentController.java`

```
GET /api/v1/payments/dev/{provider}/success?orderId=1&transactionId=1&providerTransactionId=TEST123&signature=test
```

**Response:**
```json
{
  "code": 200,
  "message": "Payment successful",
  "data": null
}
```

---

### 6. Dev Payment Fail (Testing)
**Controller:** `DevPaymentController.java`

```
GET /api/v1/payments/dev/{provider}/fail?orderId=1&transactionId=1&providerTransactionId=TEST123&signature=test
```

**Response:**
```json
{
  "code": 200,
  "message": "Payment failed",
  "data": null
}
```

---

## Shop APIs

### 1. Get Shop Detail
**Controller:** `ShopPublicController.java` - `/api/v1/storefront/shops`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/storefront/controller/ShopPublicController.java`

```
GET /api/v1/storefront/shops/{shopId}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get shop detail successfully",
  "data": {
    "id": 1,
    "name": "Shop Name",
    "description": "Shop description",
    "avatar": "https://...",
    "followerCount": 100,
    "rating": 4.5,
    "productCount": 50
  }
}
```

**Service:** `ShopPublicService.java`
**DTO:** `ShopPublicResponse`

---

### 2. Get Shop Products
**Controller:** `ShopPublicController.java`

```
GET /api/v1/storefront/shops/{shopId}/products?page=1&size=20
```

**Response:**
```json
{
  "code": 200,
  "message": "Get shop products successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Product Name",
        "price": 99.99,
        "image": "https://...",
        "sold": 10,
        "rating": 4.5
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

**DTO:** `ShopProductResponse`

---

### 3. Update Shop Info
**Controller:** `ShopManagementController.java` - `/api/v1/shops`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/identity/controller/ShopManagementController.java`

```
PUT /api/v1/shops/update
```

**Headers:**
```
Authorization: Bearer {seller_token}
```

**Request Body:**
```json
{
  "shopId": 1,
  "shopName": "Updated Shop Name",
  "description": "Updated description",
  "avatar": "https://...",
  "bannerImage": "https://..."
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Shop updated successfully",
  "data": {
    "id": 1,
    "shopName": "Updated Shop Name"
  }
}
```

**Service:** `ShopManagerService.java`
**DTO:** `ShopManagerRequest`

---

### 4. Get Shop Info
**Controller:** `ShopManagementController.java`

```
GET /api/v1/shops/info
```

**Headers:**
```
Authorization: Bearer {seller_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get shop info successfully",
  "data": {
    "id": 1,
    "name": "Shop Name",
    "description": "Description",
    "avatar": "https://...",
    "status": "ACTIVE"
  }
}
```

---

## Voucher APIs

### 1. Get Shop Vouchers
**Controller:** `BuyerVoucherController.java` - `/api/v1/vouchers`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/finance_core/controller/BuyerVoucherController.java`

```
GET /api/v1/vouchers/shop/{shopId}?orderValue=100
```

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get vouchers successfully",
  "data": [
    {
      "id": 1,
      "code": "SAVE20",
      "discountPercentage": 20,
      "minOrderValue": 50,
      "maxDiscount": 100,
      "expiryDate": "2024-12-31",
      "isUsed": false
    }
  ]
}
```

**Service:** `VoucherService.java`
**DTO:** `ShopVoucherResponse`

---

### 2. Get Seller Vouchers
**Controller:** `VoucherV2Controller.java` - `/api/v2/seller/vouchers`
**File Path:** `src/main/java/com/gr6/SmartCart/module_v2/promotion/controller/VoucherV2Controller.java`

```
GET /api/v2/seller/vouchers
```

**Headers:**
```
Authorization: Bearer {seller_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get vouchers successfully",
  "data": [
    {
      "id": 1,
      "code": "SAVE20",
      "discountPercentage": 20,
      "minOrderValue": 50,
      "status": "ACTIVE"
    }
  ]
}
```

**Service:** `VoucherV2Service.java`
**DTO:** `VoucherResponse`

---

### 3. Create Voucher
**Controller:** `VoucherV2Controller.java`

```
POST /api/v2/seller/vouchers
```

**Request Body:**
```json
{
  "code": "SAVE20",
  "discountType": "PERCENTAGE",
  "discountValue": 20,
  "minOrderValue": 50,
  "maxDiscount": 100,
  "quantity": 100,
  "expiryDate": "2024-12-31"
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Voucher created successfully",
  "data": {
    "id": 1,
    "code": "SAVE20",
    "discountPercentage": 20
  }
}
```

**DTO:** `VoucherRequest`, `VoucherResponse`
**Database Tables:** `vouchers`

---

### 4. Update Voucher
**Controller:** `VoucherV2Controller.java`

```
PUT /api/v2/seller/vouchers/{voucherId}
```

**Request Body:**
```json
{
  "code": "SAVE30",
  "discountValue": 30
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Voucher updated successfully",
  "data": {
    "id": 1,
    "code": "SAVE30",
    "discountPercentage": 30
  }
}
```

---

### 5. Deactivate Voucher
**Controller:** `VoucherV2Controller.java`

```
DELETE /api/v2/seller/vouchers/{voucherId}
```

**Response:**
```json
{
  "code": 200,
  "message": "Voucher deactivated successfully",
  "data": null
}
```

---

## Chat APIs

### 1. Get Conversations (REST)
**Controller:** `ChatRestController.java` - `/api/v1/chat`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/chat/controller/ChatRestController.java`

```
GET /api/v1/chat/conversations
```

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get conversations successfully",
  "data": [
    {
      "conversationId": 1,
      "partnerId": 2,
      "partnerName": "Partner Name",
      "partnerAvatar": "https://...",
      "lastMessage": "Hello",
      "lastMessageTime": "2024-01-01T10:00:00",
      "unreadCount": 2
    }
  ]
}
```

**Service:** `ChatService.java`
**DTO:** `ConversationResponse`

---

### 2. Get Messages with User
**Controller:** `ChatRestController.java`

```
GET /api/v1/chat/messages/{partnerId}?page=0&size=20
```

**Response:**
```json
{
  "code": 200,
  "message": "Get messages successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "senderId": 1,
        "receiverId": 2,
        "message": "Hello",
        "timestamp": "2024-01-01T10:00:00",
        "isRead": true
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

**DTO:** `ChatMessageResponse`
**Database Tables:** `chat_messages`, `conversations`

---

### 3. Mark Messages as Read
**Controller:** `ChatRestController.java`

```
PATCH /api/v1/chat/messages/{partnerId}/read
```

**Response:**
```json
{
  "code": 200,
  "message": "Marked as read",
  "data": 2
}
```

---

### 4. Send Chat Message (WebSocket)
**Controller:** `ChatWebSocketController.java`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/chat/controller/ChatWebSocketController.java`

**WebSocket Connection:**
```
WS: ws://localhost:8080/ws
```

**Subscribe to:**
```
/user/queue/messages
```

**Send Message:**
```
/app/chat.send
```

**Message Format:**
```json
{
  "receiverId": 2,
  "message": "Hello there",
  "conversationId": 1
}
```

**Response (broadcast to both users):**
```json
{
  "id": 1,
  "senderId": 1,
  "receiverId": 2,
  "message": "Hello there",
  "timestamp": "2024-01-01T10:00:00",
  "isRead": false
}
```

**Service:** `ChatService.java`
**DTO:** `ChatMessageRequest`, `ChatMessageResponse`

---

## Fulfillment APIs

### 1. Get All Shop Orders
**Controller:** `OrderQueryController.java` - `/api/v1/shop-orders`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/fulfillment/controller/OrderQueryController.java`

```
GET /api/v1/shop-orders?keyword=
```

**Headers:**
```
Authorization: Bearer {seller_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get orders successfully",
  "data": [
    {
      "id": 1,
      "orderId": 1,
      "customerName": "Customer Name",
      "itemCount": 2,
      "totalAmount": 228.98,
      "status": "PENDING_CONFIRMATION",
      "createdAt": "2024-01-01T10:00:00"
    }
  ]
}
```

**Service:** `OrderQueryService.java`
**DTO:** `OrderListResponse`

---

### 2. Get Order Detail
**Controller:** `OrderQueryController.java`

```
GET /api/v1/shop-orders/{id}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get order detail successfully",
  "data": {
    "id": 1,
    "orderId": 1,
    "customerName": "Customer Name",
    "customerPhone": "0987654321",
    "deliveryAddress": "123 Main St, Ward 1, District 1, City",
    "items": [
      {
        "productId": 1,
        "productName": "Product",
        "variantId": 1,
        "quantity": 2,
        "price": 99.99,
        "total": 199.98
      }
    ],
    "subtotal": 199.98,
    "shippingCost": 10.00,
    "tax": 19.00,
    "totalAmount": 228.98,
    "status": "PENDING_CONFIRMATION",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

**DTO:** `OrderDetailResponse`

---

### 3. Confirm Order
**Controller:** `OrderActionController.java` - `/api/v1/shop-orders`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/fulfillment/controller/OrderActionController.java`

```
PUT /api/v1/shop-orders/{id}/confirm
```

**Response:**
```json
{
  "code": 200,
  "message": "Order confirmed successfully",
  "data": null
}
```

**Service:** `OrderActionService.java`

---

### 4. Update Shop Order Status
**Controller:** `OrderActionController.java`

```
PUT /api/v1/shop-orders/{shopOrderId}/status
```

**Request Body:**
```json
{
  "status": "SHIPPED",
  "trackingNumber": "TRACK123456"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Order status updated successfully",
  "data": null
}
```

**DTO:** `UpdateShopOrderStatusRequest`

---

### 5. Cancel Order
**Controller:** `OrderActionController.java`

```
PUT /api/v1/shop-orders/{id}/ 
```

**Request Body:**
```json
{
  "reason": "Out of stock",
  "refundAmount": 228.98
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Order cancelled successfully",
  "data": null
}
```

**DTO:** `CancelOrderRequest`

---

## Order Tracking & Refund APIs

### 1. Get Order History
**Controller:** `TrackingController.java` - `/api/v2/orders/tracking`
**File Path:** `src/main/java/com/gr6/SmartCart/module_v2/order_v2/controller/TrackingController.java`

```
GET /api/v2/orders/tracking/history
```

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get order history successfully",
  "data": [
    {
      "id": 1,
      "orderId": 1,
      "customerName": "Customer",
      "status": "DELIVERED",
      "createdAt": "2024-01-01T10:00:00",
      "deliveredAt": "2024-01-05T15:30:00"
    }
  ]
}
```

**Service:** `TrackingService.java`
**DTO:** `OrderHistoryResponse`

---

### 2. Track Order
**Controller:** `TrackingController.java`

```
GET /api/v2/orders/tracking/{shopOrderId}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get tracking information successfully",
  "data": {
    "id": 1,
    "orderId": 1,
    "status": "SHIPPED",
    "statusHistory": [
      {
        "status": "PENDING_CONFIRMATION",
        "timestamp": "2024-01-01T10:00:00"
      },
      {
        "status": "CONFIRMED",
        "timestamp": "2024-01-01T11:00:00"
      },
      {
        "status": "SHIPPED",
        "timestamp": "2024-01-02T09:00:00"
      }
    ],
    "trackingNumber": "TRACK123456",
    "estimatedDelivery": "2024-01-05"
  }
}
```

**DTO:** `OrderTrackingResponse`

---

### 3. Cancel Order (Buyer)
**Controller:** `RefundController.java` - `/api/v2/orders/refund`
**File Path:** `src/main/java/com/gr6/SmartCart/module_v2/order_v2/controller/RefundController.java`

```
POST /api/v2/orders/refund/{shopOrderId}/cancel
```

**Request Body:**
```json
{
  "reason": "Product quality issue",
  "description": "The product doesn't match the description"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Refund request submitted successfully",
  "data": null
}
```

**Service:** `RefundService.java`
**DTO:** `RefundRequest`
**Database Tables:** `refunds`

---

## Withdraw & Settlement APIs

### 1. Get Seller Wallet
**Controller:** `SellerWithdrawController.java` - `/api/v3/seller/withdraw`
**File Path:** `src/main/java/com/gr6/SmartCart/module_v3/withdraw/controller/SellerWithdrawController.java`

```
GET /api/v3/seller/withdraw/wallet
```

**Headers:**
```
Authorization: Bearer {seller_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get wallet summary successfully",
  "data": {
    "totalBalance": 5000.00,
    "availableBalance": 4500.00,
    "pendingBalance": 500.00,
    "withdrawnTotal": 10000.00,
    "lastWithdrawDate": "2024-01-10"
  }
}
```

**Service:** `SellerWithdrawService.java`
**DTO:** `WalletSummaryResponse`

---

### 2. Get Wallet Transactions
**Controller:** `SellerWithdrawController.java`

```
GET /api/v3/seller/withdraw/wallet/transactions?page=1&size=10
```

**Response:**
```json
{
  "code": 200,
  "message": "Get wallet transactions successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "type": "ORDER_COMMISSION",
        "amount": 100.00,
        "description": "Commission from order #123",
        "date": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 5
  }
}
```

**DTO:** `WalletTransactionResponse`

---

### 3. Create Withdraw Request
**Controller:** `SellerWithdrawController.java`

```
POST /api/v3/seller/withdraw/requests
```

**Request Body:**
```json
{
  "amount": 1000.00,
  "bankAccountId": 1,
  "note": "Monthly withdrawal"
}
```

**Response:**
```json
{
  "code": 201,
  "message": "Withdraw request created successfully",
  "data": {
    "id": 1,
    "amount": 1000.00,
    "status": "PENDING",
    "createdAt": "2024-01-15T10:00:00"
  }
}
```

**DTO:** `WithdrawCreateRequest`, `WithdrawResponse`
**Database Tables:** `withdrawals`

---

### 4. Get Withdraw Requests
**Controller:** `SellerWithdrawController.java`

```
GET /api/v3/seller/withdraw/requests?page=1&size=10
```

**Response:**
```json
{
  "code": 200,
  "message": "Get withdraw requests successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "amount": 1000.00,
        "status": "APPROVED",
        "bankAccount": "1234567890",
        "createdAt": "2024-01-15T10:00:00",
        "approvedAt": "2024-01-16T09:00:00"
      }
    ],
    "totalElements": 20,
    "totalPages": 2
  }
}
```

---

### 5. Get Settlements
**Controller:** `SellerWithdrawController.java`

```
GET /api/v3/seller/withdraw/settlements?page=1&size=10
```

**Response:**
```json
{
  "code": 200,
  "message": "Get settlements successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "settlementDate": "2024-01-01",
        "totalSales": 5000.00,
        "totalCommission": 500.00,
        "refunds": 100.00,
        "netAmount": 4400.00,
        "status": "SETTLED"
      }
    ],
    "totalElements": 12,
    "totalPages": 2
  }
}
```

**DTO:** `SellerSettlementResponse`

---

### 6. Admin Reconcile Orders
**Controller:** `AdminWithdrawController.java` - `/api/v3/admin/withdraw`
**File Path:** `src/main/java/com/gr6/SmartCart/module_v3/withdraw/controller/AdminWithdrawController.java`

```
POST /api/v3/admin/withdraw/reconcile
```

**Headers:**
```
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Reconciliation completed successfully",
  "data": {
    "processedOrders": 150,
    "totalAmount": 50000.00,
    "createdsettlements": 10
  }
}
```

**Service:** `AdminWithdrawService.java`
**DTO:** `ReconcileResponse`

---

### 7. Admin Get Settlements
**Controller:** `AdminWithdrawController.java`

```
GET /api/v3/admin/withdraw/settlements?page=1&size=10
```

**Response:**
```json
{
  "code": 200,
  "message": "Get settlements successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "shopId": 1,
        "shopName": "Shop Name",
        "settlementDate": "2024-01-01",
        "totalAmount": 4400.00,
        "status": "SETTLED"
      }
    ],
    "totalElements": 100,
    "totalPages": 10
  }
}
```

---

### 8. Admin Get Withdraw Requests
**Controller:** `AdminWithdrawController.java`

```
GET /api/v3/admin/withdraw/requests?status=PENDING&page=1&size=10
```

**Response:**
```json
{
  "code": 200,
  "message": "Get withdraw requests successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "shopId": 1,
        "shopName": "Shop Name",
        "amount": 1000.00,
        "bankAccount": "1234567890",
        "status": "PENDING",
        "createdAt": "2024-01-15T10:00:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 3
  }
}
```

---

### 9. Admin Approve Withdraw
**Controller:** `AdminWithdrawController.java`

```
PATCH /api/v3/admin/withdraw/requests/{withdrawId}/approve
```

**Request Body (optional):**
```json
{
  "note": "Approved",
  "approvalDate": "2024-01-16T09:00:00"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Withdraw approved successfully",
  "data": {
    "id": 1,
    "status": "APPROVED"
  }
}
```

**DTO:** `AdminWithdrawDecisionRequest`

---

### 10. Admin Reject Withdraw
**Controller:** `AdminWithdrawController.java`

```
PATCH /api/v3/admin/withdraw/requests/{withdrawId}/reject
```

**Request Body (optional):**
```json
{
  "rejectionReason": "Account information mismatch",
  "rejectionDate": "2024-01-16T09:00:00"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Withdraw rejected successfully",
  "data": {
    "id": 1,
    "status": "REJECTED"
  }
}
```

---

## Admin APIs

### 1. Get Users (Admin)
**Controller:** `AdminAccountController.java` - `/api/v1/admin`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/identity/controller/AdminAccountController.java`

```
GET /api/v1/admin/users?page=1&size=10&role=BUYER&status=ACTIVE&keyword=
```

**Headers:**
```
Authorization: Bearer {admin_token}
```

**Response:**
```json
{
  "code": 200,
  "message": "Get users successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user@example.com",
        "fullName": "User Name",
        "role": "BUYER",
        "status": "ACTIVE",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10
  }
}
```

**Service:** `AdminAccountService.java`
**DTO:** `UserAdminResponse`

---

### 2. Ban User
**Controller:** `AdminAccountController.java`

```
PATCH /api/v1/admin/users/{userId}/ban
```

**Response:**
```json
{
  "code": 200,
  "message": "User banned successfully",
  "data": null
}
```

---

### 3. Unban User
**Controller:** `AdminAccountController.java`

```
PATCH /api/v1/admin/users/{userId}/unban
```

**Response:**
```json
{
  "code": 200,
  "message": "User unbanned successfully",
  "data": null
}
```

---

### 4. Get Shops (Admin)
**Controller:** `AdminAccountController.java`

```
GET /api/v1/admin/shops?page=1&size=10&status=APPROVED&keyword=
```

**Response:**
```json
{
  "code": 200,
  "message": "Get shops successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "shopName": "Shop Name",
        "ownerEmail": "owner@example.com",
        "status": "APPROVED",
        "createdAt": "2024-01-01T10:00:00",
        "productCount": 50
      }
    ],
    "totalElements": 50,
    "totalPages": 5
  }
}
```

**DTO:** `ShopAdminResponse`

---

### 5. Approve Shop
**Controller:** `AdminAccountController.java`

```
PATCH /api/v1/admin/shops/{shopId}/approve
```

**Response:**
```json
{
  "code": 200,
  "message": "Shop approved successfully",
  "data": null
}
```

---

### 6. Reject Shop
**Controller:** `AdminAccountController.java`

```
PATCH /api/v1/admin/shops/{shopId}/reject?reason=Reason
```

**Response:**
```json
{
  "code": 200,
  "message": "Shop rejected successfully",
  "data": null
}
```

---

### 7. Ban Shop
**Controller:** `AdminAccountController.java`

```
PATCH /api/v1/admin/shops/{shopId}/ban?reason=Reason
```

**Response:**
```json
{
  "code": 200,
  "message": "Shop banned successfully",
  "data": null
}
```

---

### 8. Unban Shop
**Controller:** `AdminAccountController.java`

```
PATCH /api/v1/admin/shops/{shopId}/unban
```

**Response:**
```json
{
  "code": 200,
  "message": "Shop unbanned successfully",
  "data": null
}
```

---

## Storefront Discovery APIs

### 1. Get Home Products
**Controller:** `DiscoveryController.java` - `/api/v1/storefront/discovery`
**File Path:** `src/main/java/com/gr6/SmartCart/modules/storefront/controller/DiscoveryController.java`

```
GET /api/v1/storefront/discovery/home-products
```

**Response:**
```json
{
  "code": 200,
  "message": "Get home products successfully",
  "data": [
    {
      "id": 1,
      "name": "Featured Product",
      "price": 99.99,
      "image": "https://...",
      "rating": 4.5,
      "sold": 100
    }
  ]
}
```

**Service:** `DiscoveryService.java`
**DTO:** `ProductResponseDTO`

---

### 2. Search & Filter Products
**Controller:** `DiscoveryController.java`

```
POST /api/v1/storefront/discovery/search?page=0&size=20
```

**Request Body:**
```json
{
  "keyword": "laptop",
  "categoryId": 1,
  "minPrice": 100,
  "maxPrice": 5000,
  "rating": 4,
  "sortBy": "RELEVANCE",
  "sortOrder": "DESC"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Search successful",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Product Name",
        "price": 99.99,
        "image": "https://...",
        "shopName": "Shop Name",
        "rating": 4.5
      }
    ],
    "totalElements": 100,
    "totalPages": 5
  }
}
```

**DTO:** `SearchFilterRequest`, `ProductResponseDTO`

---

## Project Structure Overview

```
src/main/java/com/gr6/SmartCart/
├── SmartCartApplication.java
├── common/
│   ├── base/
│   │   ├── BaseResponse.java (통합 응답 포맷)
│   │   └── PageResponse.java (페이징 응답)
│   ├── domain/
│   │   ├── Address.java
│   │   ├── CartItem.java
│   │   ├── Category.java
│   │   └── ... (기타 도메인 모델)
│   ├── enums/ (열거형 타입들)
│   ├── exception/ (예외 처리)
│   └── security/ (보안 설정)
│
├── modules/
│   ├── catalog/ (상품 관리)
│   │   ├── controller/
│   │   │   ├── AdminProductController.java
│   │   │   ├── CategoryController.java
│   │   │   ├── InventoryController.java
│   │   │   ├── ProductController.java
│   │   │   └── ProductVariantController.java
│   │   ├── service/ (비즈니스 로직)
│   │   ├── dto/ (데이터 전송 객체)
│   │   ├── repository/ (데이터베이스 접근)
│   │   └── entity/ (JPA 엔티티)
│   │
│   ├── storefront/ (상점 정보, 검색)
│   │   ├── controller/
│   │   │   ├── CartController.java
│   │   │   ├── DiscoveryController.java
│   │   │   └── ShopPublicController.java
│   │   ├── service/
│   │   ├── dto/
│   │   └── repository/
│   │
│   ├── finance_core/ (주문 & 결제)
│   │   ├── controller/
│   │   │   ├── BuyerVoucherController.java
│   │   │   ├── DevPaymentController.java
│   │   │   ├── OrderController.java
│   │   │   └── PaymentReturnController.java
│   │   ├── service/
│   │   ├── dto/
│   │   ├── repository/
│   │   ├── util/ (결제 암호화 등)
│   │   └── entity/
│   │
│   ├── chat/ (실시간 채팅)
│   │   ├── controller/
│   │   │   ├── ChatRestController.java
│   │   │   └── ChatWebSocketController.java
│   │   ├── service/
│   │   ├── dto/
│   │   ├── config/ (WebSocket 설정)
│   │   ├── repository/
│   │   └── entity/
│   │
│   ├── fulfillment/ (주문 이행)
│   │   ├── controller/
│   │   │   ├── OrderActionController.java
│   │   │   └── OrderQueryController.java
│   │   ├── service/
│   │   ├── dto/
│   │   ├── repository/
│   │   └── entity/
│   │
│   └── identity/ (인증, 계정 관리)
│       ├── controller/
│       │   ├── AdminAccountController.java
│       │   ├── LoginController.java
│       │   ├── RegistrationController.java
│       │   ├── ShopManagementController.java
│       │   └── ShopRegistrationController.java
│       ├── service/
│       ├── dto/
│       ├── repository/
│       └── entity/
│
├── module_v2/ (v2 API들)
│   ├── auth/
│   │   └── controller/ PasswordController.java
│   │
│   ├── order_v2/ (주문 추적, 환불)
│   │   ├── controller/
│   │   │   ├── RefundController.java
│   │   │   └── TrackingController.java
│   │   ├── service/
│   │   ├── dto/
│   │   └── repository/
│   │
│   ├── promotion/ (쿠폰/할인)
│   │   ├── controller/ VoucherV2Controller.java
│   │   ├── service/
│   │   ├── dto/
│   │   └── repository/
│   │
│   └── user/ (사용자 프로필)
│       ├── controller/
│       │   ├── AddressController.java
│       │   └── ProfileController.java
│       ├── service/
│       ├── dto/
│       └── repository/
│
└── module_v3/ (v3 API들)
    ├── analytics/ (분석)
    │   └── (currently empty)
    │
    └── withdraw/ (정산, 출금)
        ├── controller/
        │   ├── AdminWithdrawController.java
        │   └── SellerWithdrawController.java
        ├── service/
        ├── dto/
        ├── repository/
        └── entity/
```

---

## Common Response Format

모든 API는 다음과 같은 통일된 응답 형식을 사용합니다:

```json
{
  "code": 200,
  "message": "Success message",
  "data": {} or [] or null
}
```

**상태 코드:**
- `200`: OK - 요청 성공
- `201`: Created - 리소스 생성 성공
- `400`: Bad Request - 잘못된 요청
- `401`: Unauthorized - 인증 필요
- `403`: Forbidden - 접근 권한 없음
- `404`: Not Found - 리소스 없음
- `500`: Internal Server Error - 서버 오류

---

## Base Response Utility

**파일:** `src/main/java/com/gr6/SmartCart/common/base/BaseResponse.java`

### 주요 메서드:

```java
// 성공 응답 (데이터 포함)
BaseResponse.success_data("메시지", data)

// 성공 응답 (메시지만)
BaseResponse.successMessage("메시지")

// 성공 응답
BaseResponse.success("메시지")

// 에러 응답
BaseResponse.error(400, "에러 메시지")
```

---

## 인증 방식

### JWT Token

모든 보호된 API는 다음 헤더를 필요로 합니다:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Token 유효성:**
- Login 후 획득 가능
- 요청 헤더에 `Authorization: Bearer {token}` 형식으로 포함
- 토큰 만료 시 401 Unauthorized 응답

---

## 데이터베이스 테이블 주요 구조

### Users (사용자)
- `userId` (PK)
- `email` (UNIQUE)
- `password` (암호화)
- `fullName`
- `phone`
- `avatar`
- `role` (BUYER, SELLER, ADMIN)
- `status` (ACTIVE, BANNED)
- `createdAt`

### Shops (상점)
- `shopId` (PK)
- `userId` (FK)
- `shopName`
- `description`
- `avatar`
- `status` (PENDING, APPROVED, REJECTED, BANNED)
- `createdAt`

### Products (상품)
- `productId` (PK)
- `shopId` (FK)
- `categoryId` (FK)
- `name`
- `description`
- `price`
- `status` (ACTIVE, BANNED, DELETED)
- `createdAt`

### Product_Variants (상품 옵션)
- `variantId` (PK)
- `productId` (FK)
- `color`
- `size`
- `price`
- `stock`
- `sku`

### Carts (장바구니)
- `cartId` (PK)
- `userId` (FK)
- `createdAt`

### Cart_Items (장바구니 아이템)
- `cartItemId` (PK)
- `cartId` (FK)
- `variantId` (FK)
- `quantity`

### Orders (주문)
- `orderId` (PK)
- `userId` (FK)
- `totalAmount`
- `status` (PENDING_PAYMENT, PAID, CANCELLED)
- `createdAt`

### Shop_Orders (상점별 주문)
- `shopOrderId` (PK)
- `orderId` (FK)
- `shopId` (FK)
- `status` (PENDING_CONFIRMATION, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- `createdAt`

### Order_Items (주문 아이템)
- `orderItemId` (PK)
- `shopOrderId` (FK)
- `variantId` (FK)
- `quantity`
- `price`

### Addresses (배송 주소)
- `addressId` (PK)
- `userId` (FK)
- `street`
- `ward`
- `district`
- `province`
- `isDefault`

### Vouchers (쿠폰)
- `voucherId` (PK)
- `shopId` (FK)
- `code`
- `discountType` (PERCENTAGE, FIXED_AMOUNT)
- `discountValue`
- `minOrderValue`
- `maxDiscount`
- `status` (ACTIVE, DEACTIVATED)
- `expiryDate`

### Chat_Messages (채팅 메시지)
- `messageId` (PK)
- `conversationId` (FK)
- `senderId` (FK)
- `receiverId` (FK)
- `message`
- `isRead`
- `timestamp`

### Conversations (대화)
- `conversationId` (PK)
- `userId1` (FK)
- `userId2` (FK)
- `lastMessageAt`

### Withdrawals (출금)
- `withdrawId` (PK)
- `shopId` (FK)
- `amount`
- `bankAccount`
- `status` (PENDING, APPROVED, REJECTED, COMPLETED)
- `createdAt`

### Settlements (정산)
- `settlementId` (PK)
- `shopId` (FK)
- `settlementDate`
- `totalSales`
- `totalCommission`
- `refunds`
- `netAmount`
- `status` (PENDING, SETTLED)

---

## 주요 Service 클래스들

| Service | 역할 |
|---------|------|
| `LoginService` | 로그인 처리, JWT 발급 |
| `RegisterService` | 회원가입 |
| `ProfileService` | 사용자 프로필 조회/수정 |
| `AddressService` | 배송 주소 관리 |
| `ProductService` | 상품 CRUD |
| `CategoryService` | 카테고리 관리 |
| `ProductVariantService` | 상품 옵션 관리 |
| `InventoryService` | 재고 관리 |
| `CartService` | 장바구니 관리 |
| `OrderService` | 주문 생성, 결제 콜백 처리 |
| `OrderQueryService` | 판매자 주문 조회 |
| `OrderActionService` | 판매자 주문 상태 변경 |
| `ChatService` | 채팅 메시지 관리 |
| `VoucherService` | 구매자 쿠폰 조회 |
| `VoucherV2Service` | 판매자 쿠폰 관리 |
| `SellerWithdrawService` | 판매자 출금 요청 |
| `AdminWithdrawService` | 관리자 정산 관리 |
| `ShopPublicService` | 공개 상점 정보 |
| `DiscoveryService` | 상품 검색/필터링 |
| `AdminAccountService` | 관리자 계정 관리 |
| `ShopManagerService` | 상점 정보 관리 |

---

## 주요 DTO (Data Transfer Object) 클래스들

| DTO | 용도 |
|-----|------|
| `LoginRequest` | 로그인 요청 |
| `RegisterRequest` | 회원가입 요청 |
| `ProfileDTO` | 사용자 프로필 |
| `AddressRequestDTO`, `AddressResponseDTO` | 주소 관리 |
| `ProductRequest`, `ProductResponse` | 상품 정보 |
| `CategoryRequest`, `CategoryResponse` | 카테고리 정보 |
| `VariantCreateRequest`, `VariantResponse` | 상품 옵션 |
| `CartItemRequest`, `CartDetailResponseDTO` | 장바구니 |
| `CreateOrderRequest`, `CheckoutPreviewResponse` | 주문 생성 |
| `ChatMessageRequest`, `ChatMessageResponse` | 채팅 메시지 |
| `VoucherRequest`, `VoucherResponse` | 쿠폰 정보 |
| `WithdrawCreateRequest`, `WithdrawResponse` | 출금 요청 |
| `UserAdminResponse`, `ShopAdminResponse` | 관리자 조회 |

이 문서는 SmartCart 애플리케이션의 모든 API 엔드포인트, 요청/응답 형식, 그리고 처리하는 파일들을 상세히 기술합니다.
