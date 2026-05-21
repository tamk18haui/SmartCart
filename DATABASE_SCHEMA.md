# SmartCart Database Schema

## Bảng Dữ Liệu Chính

### 1. 👤 USERS - Bảng Người Dùng

```sql
CREATE TABLE users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(255),
  phone VARCHAR(20),
  avatar VARCHAR(500),
  role VARCHAR(20),                -- BUYER, SELLER, ADMIN
  status VARCHAR(20),               -- ACTIVE, BANNED
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_role (role),
  INDEX idx_status (status)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `user_id` | BIGINT | ID duy nhất |
| `email` | VARCHAR(255) | Email đăng nhập (unique) |
| `password` | VARCHAR(255) | Password đã hash |
| `full_name` | VARCHAR(255) | Tên đầy đủ |
| `phone` | VARCHAR(20) | Số điện thoại |
| `avatar` | VARCHAR(500) | URL ảnh đại diện |
| `role` | VARCHAR(20) | Vai trò (BUYER/SELLER/ADMIN) |
| `status` | VARCHAR(20) | Trạng thái (ACTIVE/BANNED) |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 2. 🏪 SHOPS - Bảng Cửa Hàng

```sql
CREATE TABLE shops (
  shop_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  shop_name VARCHAR(255) NOT NULL,
  description TEXT,
  avatar VARCHAR(500),
  banner_image VARCHAR(500),
  status VARCHAR(20),               -- PENDING, APPROVED, REJECTED, BANNED
  rating DECIMAL(3,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  INDEX idx_user_id (user_id),
  INDEX idx_status (status),
  UNIQUE KEY uk_user_id (user_id)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `shop_id` | BIGINT | ID duy nhất |
| `user_id` | BIGINT | ID chủ cửa hàng (FK) |
| `shop_name` | VARCHAR(255) | Tên cửa hàng |
| `description` | TEXT | Mô tả cửa hàng |
| `avatar` | VARCHAR(500) | Logo cửa hàng |
| `banner_image` | VARCHAR(500) | Ảnh banner |
| `status` | VARCHAR(20) | Trạng thái duyệt |
| `rating` | DECIMAL(3,2) | Đánh giá trung bình (0-5) |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 3. 📂 CATEGORIES - Bảng Danh Mục

```sql
CREATE TABLE categories (
  category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  image VARCHAR(500),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_is_active (is_active),
  INDEX idx_name (name)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `category_id` | BIGINT | ID duy nhất |
| `name` | VARCHAR(255) | Tên danh mục (unique) |
| `description` | TEXT | Mô tả danh mục |
| `image` | VARCHAR(500) | Ảnh danh mục |
| `is_active` | BOOLEAN | Trạng thái hoạt động |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 4. 📦 PRODUCTS - Bảng Sản Phẩm

```sql
CREATE TABLE products (
  product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  base_price DECIMAL(10,2),
  status VARCHAR(20),               -- ACTIVE, BANNED, DELETED
  sold_count INT DEFAULT 0,
  rating DECIMAL(3,2) DEFAULT 0,
  review_count INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (shop_id) REFERENCES shops(shop_id),
  FOREIGN KEY (category_id) REFERENCES categories(category_id),
  INDEX idx_shop_id (shop_id),
  INDEX idx_category_id (category_id),
  INDEX idx_status (status),
  INDEX idx_name (name)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `product_id` | BIGINT | ID duy nhất |
| `shop_id` | BIGINT | ID cửa hàng (FK) |
| `category_id` | BIGINT | ID danh mục (FK) |
| `name` | VARCHAR(255) | Tên sản phẩm |
| `description` | TEXT | Mô tả sản phẩm |
| `base_price` | DECIMAL(10,2) | Giá cơ bản |
| `status` | VARCHAR(20) | Trạng thái (ACTIVE/BANNED/DELETED) |
| `sold_count` | INT | Số lượng đã bán |
| `rating` | DECIMAL(3,2) | Đánh giá trung bình |
| `review_count` | INT | Số đánh giá |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 5. 🎨 PRODUCT_VARIANTS - Bảng Biến Thể Sản Phẩm

```sql
CREATE TABLE product_variants (
  variant_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  color VARCHAR(100),
  size VARCHAR(100),
  sku VARCHAR(100) UNIQUE,
  price DECIMAL(10,2) NOT NULL,
  stock INT DEFAULT 0,
  image VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (product_id) REFERENCES products(product_id),
  INDEX idx_product_id (product_id),
  INDEX idx_sku (sku)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `variant_id` | BIGINT | ID duy nhất |
| `product_id` | BIGINT | ID sản phẩm (FK) |
| `color` | VARCHAR(100) | Màu sắc |
| `size` | VARCHAR(100) | Kích thước |
| `sku` | VARCHAR(100) | SKU (unique) |
| `price` | DECIMAL(10,2) | Giá bán |
| `stock` | INT | Số lượng tồn |
| `image` | VARCHAR(500) | Ảnh variant |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 6. 🖼️ PRODUCT_IMAGES - Bảng Ảnh Sản Phẩm

```sql
CREATE TABLE product_images (
  image_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  image_url VARCHAR(500) NOT NULL,
  display_order INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (product_id) REFERENCES products(product_id),
  INDEX idx_product_id (product_id)
);
```

---

### 7. 🛒 CARTS - Bảng Giỏ Hàng

```sql
CREATE TABLE carts (
  cart_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  INDEX idx_user_id (user_id)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `cart_id` | BIGINT | ID duy nhất |
| `user_id` | BIGINT | ID người dùng (FK) |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 8. 🛍️ CART_ITEMS - Bảng Mục Trong Giỏ

```sql
CREATE TABLE cart_items (
  cart_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cart_id BIGINT NOT NULL,
  variant_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (cart_id) REFERENCES carts(cart_id),
  FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
  UNIQUE KEY uk_cart_variant (cart_id, variant_id),
  INDEX idx_cart_id (cart_id),
  INDEX idx_variant_id (variant_id)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `cart_item_id` | BIGINT | ID duy nhất |
| `cart_id` | BIGINT | ID giỏ hàng (FK) |
| `variant_id` | BIGINT | ID biến thể (FK) |
| `quantity` | INT | Số lượng |
| `added_at` | TIMESTAMP | Ngày thêm |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 9. 📍 ADDRESSES - Bảng Địa Chỉ Giao Hàng

```sql
CREATE TABLE addresses (
  address_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  full_name VARCHAR(255),
  phone VARCHAR(20),
  street VARCHAR(255),
  ward VARCHAR(100),
  district VARCHAR(100),
  province VARCHAR(100),
  is_default BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  INDEX idx_user_id (user_id),
  INDEX idx_is_default (is_default)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `address_id` | BIGINT | ID duy nhất |
| `user_id` | BIGINT | ID người dùng (FK) |
| `full_name` | VARCHAR(255) | Tên nhận hàng |
| `phone` | VARCHAR(20) | Số điện thoại |
| `street` | VARCHAR(255) | Địa chỉ chi tiết |
| `ward` | VARCHAR(100) | Phường/Xã |
| `district` | VARCHAR(100) | Quận/Huyện |
| `province` | VARCHAR(100) | Tỉnh/Thành phố |
| `is_default` | BOOLEAN | Là địa chỉ mặc định? |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 10. 📋 ORDERS - Bảng Đơn Hàng Chính

```sql
CREATE TABLE orders (
  order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  address_id BIGINT,
  total_amount DECIMAL(10,2),
  status VARCHAR(20),               -- PENDING_PAYMENT, PAID, CANCELLED, COMPLETED
  shipping_method VARCHAR(50),
  payment_method VARCHAR(50),
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (address_id) REFERENCES addresses(address_id),
  INDEX idx_user_id (user_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `order_id` | BIGINT | ID duy nhất |
| `user_id` | BIGINT | ID người dùng (FK) |
| `address_id` | BIGINT | ID địa chỉ giao hàng (FK) |
| `total_amount` | DECIMAL(10,2) | Tổng tiền |
| `status` | VARCHAR(20) | Trạng thái thanh toán |
| `shipping_method` | VARCHAR(50) | Phương thức vận chuyển |
| `payment_method` | VARCHAR(50) | Phương thức thanh toán |
| `notes` | TEXT | Ghi chú |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 11. 🏪 SHOP_ORDERS - Bảng Đơn Hàng Theo Cửa Hàng

```sql
CREATE TABLE shop_orders (
  shop_order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  status VARCHAR(50),               -- PENDING_CONFIRMATION, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDING
  subtotal DECIMAL(10,2),
  shipping_cost DECIMAL(10,2),
  tax DECIMAL(10,2),
  total_amount DECIMAL(10,2),
  tracking_number VARCHAR(255),
  cancel_reason TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(order_id),
  FOREIGN KEY (shop_id) REFERENCES shops(shop_id),
  INDEX idx_order_id (order_id),
  INDEX idx_shop_id (shop_id),
  INDEX idx_status (status)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `shop_order_id` | BIGINT | ID duy nhất |
| `order_id` | BIGINT | ID đơn hàng chính (FK) |
| `shop_id` | BIGINT | ID cửa hàng (FK) |
| `status` | VARCHAR(50) | Trạng thái (PENDING/CONFIRMED/SHIPPED/etc) |
| `subtotal` | DECIMAL(10,2) | Subtotal |
| `shipping_cost` | DECIMAL(10,2) | Chi phí vận chuyển |
| `tax` | DECIMAL(10,2) | Thuế |
| `total_amount` | DECIMAL(10,2) | Tổng tiền |
| `tracking_number` | VARCHAR(255) | Mã theo dõi |
| `cancel_reason` | TEXT | Lý do hủy |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 12. 🛒 ORDER_ITEMS - Bảng Mục Trong Đơn Hàng

```sql
CREATE TABLE order_items (
  order_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_order_id BIGINT NOT NULL,
  variant_id BIGINT NOT NULL,
  product_id BIGINT,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2),
  total_price DECIMAL(10,2),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (shop_order_id) REFERENCES shop_orders(shop_order_id),
  FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
  FOREIGN KEY (product_id) REFERENCES products(product_id),
  INDEX idx_shop_order_id (shop_order_id),
  INDEX idx_variant_id (variant_id)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `order_item_id` | BIGINT | ID duy nhất |
| `shop_order_id` | BIGINT | ID đơn hàng cửa hàng (FK) |
| `variant_id` | BIGINT | ID biến thể (FK) |
| `product_id` | BIGINT | ID sản phẩm (FK) |
| `quantity` | INT | Số lượng |
| `unit_price` | DECIMAL(10,2) | Giá đơn vị |
| `total_price` | DECIMAL(10,2) | Tổng giá |
| `created_at` | TIMESTAMP | Ngày tạo |

---

### 13. 💳 PAYMENTS - Bảng Thanh Toán

```sql
CREATE TABLE payments (
  payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  provider VARCHAR(50),             -- MOMO, VNPAY, PAYPAL, CREDIT_CARD
  provider_transaction_id VARCHAR(255),
  status VARCHAR(20),               -- PENDING, SUCCESS, FAILED, REFUNDED
  signature VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(order_id),
  INDEX idx_order_id (order_id),
  INDEX idx_provider (provider),
  INDEX idx_status (status)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `payment_id` | BIGINT | ID duy nhất |
| `order_id` | BIGINT | ID đơn hàng (FK) |
| `amount` | DECIMAL(10,2) | Số tiền thanh toán |
| `provider` | VARCHAR(50) | Nhà cung cấp (MOMO/VNPAY) |
| `provider_transaction_id` | VARCHAR(255) | ID giao dịch từ provider |
| `status` | VARCHAR(20) | Trạng thái thanh toán |
| `signature` | VARCHAR(500) | Chữ ký xác minh |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 14. 🎟️ VOUCHERS - Bảng Voucher/Coupon

```sql
CREATE TABLE vouchers (
  voucher_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  code VARCHAR(100) UNIQUE NOT NULL,
  description TEXT,
  discount_type VARCHAR(20),        -- PERCENTAGE, FIXED_AMOUNT
  discount_value DECIMAL(10,2),
  min_order_value DECIMAL(10,2),
  max_discount DECIMAL(10,2),
  quantity INT DEFAULT 0,
  used_count INT DEFAULT 0,
  status VARCHAR(20),               -- ACTIVE, DEACTIVATED, EXPIRED
  start_date DATE,
  expiry_date DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (shop_id) REFERENCES shops(shop_id),
  INDEX idx_shop_id (shop_id),
  INDEX idx_code (code),
  INDEX idx_status (status)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `voucher_id` | BIGINT | ID duy nhất |
| `shop_id` | BIGINT | ID cửa hàng (FK) |
| `code` | VARCHAR(100) | Mã voucher (unique) |
| `description` | TEXT | Mô tả |
| `discount_type` | VARCHAR(20) | Loại giảm (PERCENTAGE/FIXED_AMOUNT) |
| `discount_value` | DECIMAL(10,2) | Giá trị giảm |
| `min_order_value` | DECIMAL(10,2) | Giá trị đơn tối thiểu |
| `max_discount` | DECIMAL(10,2) | Giảm tối đa |
| `quantity` | INT | Số lượng có sẵn |
| `used_count` | INT | Số lần đã dùng |
| `status` | VARCHAR(20) | Trạng thái |
| `start_date` | DATE | Ngày bắt đầu |
| `expiry_date` | DATE | Ngày hết hạn |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 15. 💬 CONVERSATIONS - Bảng Hội Thoại

```sql
CREATE TABLE conversations (
  conversation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id_1 BIGINT NOT NULL,
  user_id_2 BIGINT NOT NULL,
  last_message_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id_1) REFERENCES users(user_id),
  FOREIGN KEY (user_id_2) REFERENCES users(user_id),
  UNIQUE KEY uk_users (user_id_1, user_id_2),
  INDEX idx_user_id_1 (user_id_1),
  INDEX idx_user_id_2 (user_id_2)
);
```

---

### 16. 💌 CHAT_MESSAGES - Bảng Tin Nhắn

```sql
CREATE TABLE chat_messages (
  message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  conversation_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  message TEXT NOT NULL,
  is_read BOOLEAN DEFAULT FALSE,
  read_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id),
  FOREIGN KEY (sender_id) REFERENCES users(user_id),
  FOREIGN KEY (receiver_id) REFERENCES users(user_id),
  INDEX idx_conversation_id (conversation_id),
  INDEX idx_sender_id (sender_id),
  INDEX idx_receiver_id (receiver_id),
  INDEX idx_is_read (is_read)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `message_id` | BIGINT | ID duy nhất |
| `conversation_id` | BIGINT | ID hội thoại (FK) |
| `sender_id` | BIGINT | ID người gửi (FK) |
| `receiver_id` | BIGINT | ID người nhận (FK) |
| `message` | TEXT | Nội dung tin nhắn |
| `is_read` | BOOLEAN | Đã đọc? |
| `read_at` | TIMESTAMP | Thời gian đọc |
| `created_at` | TIMESTAMP | Ngày gửi |

---

### 17. 🔄 REFUNDS - Bảng Hoàn Hàng

```sql
CREATE TABLE refunds (
  refund_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  shop_order_id BIGINT,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10,2),
  reason VARCHAR(255),
  description TEXT,
  status VARCHAR(20),               -- PENDING, APPROVED, REJECTED, COMPLETED
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(order_id),
  FOREIGN KEY (shop_order_id) REFERENCES shop_orders(shop_order_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  INDEX idx_order_id (order_id),
  INDEX idx_status (status)
);
```

---

### 18. 👛 WALLETS - Bảng Ví Bán Hàng

```sql
CREATE TABLE wallets (
  wallet_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL UNIQUE,
  total_balance DECIMAL(15,2) DEFAULT 0,
  available_balance DECIMAL(15,2) DEFAULT 0,
  pending_balance DECIMAL(15,2) DEFAULT 0,
  withdrawn_total DECIMAL(15,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (shop_id) REFERENCES shops(shop_id),
  INDEX idx_shop_id (shop_id)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `wallet_id` | BIGINT | ID duy nhất |
| `shop_id` | BIGINT | ID cửa hàng (FK) |
| `total_balance` | DECIMAL(15,2) | Tổng số dư |
| `available_balance` | DECIMAL(15,2) | Số dư có sẵn |
| `pending_balance` | DECIMAL(15,2) | Số dư chờ xử lý |
| `withdrawn_total` | DECIMAL(15,2) | Tổng đã rút |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 19. 💸 WALLET_TRANSACTIONS - Bảng Giao Dịch Ví

```sql
CREATE TABLE wallet_transactions (
  transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  wallet_id BIGINT NOT NULL,
  shop_id BIGINT NOT NULL,
  type VARCHAR(50),                 -- ORDER_COMMISSION, REFUND, ADJUSTMENT, WITHDRAWAL
  amount DECIMAL(15,2),
  description TEXT,
  reference_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id),
  FOREIGN KEY (shop_id) REFERENCES shops(shop_id),
  INDEX idx_wallet_id (wallet_id),
  INDEX idx_shop_id (shop_id),
  INDEX idx_type (type)
);
```

---

### 20. 💰 WITHDRAWALS - Bảng Yêu Cầu Rút Tiền

```sql
CREATE TABLE withdrawals (
  withdrawal_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  wallet_id BIGINT NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  bank_account_id BIGINT,
  bank_account_number VARCHAR(50),
  bank_name VARCHAR(255),
  account_name VARCHAR(255),
  status VARCHAR(20),               -- PENDING, APPROVED, REJECTED, COMPLETED
  rejection_reason TEXT,
  processed_by BIGINT,
  processed_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (shop_id) REFERENCES shops(shop_id),
  FOREIGN KEY (wallet_id) REFERENCES wallets(wallet_id),
  FOREIGN KEY (processed_by) REFERENCES users(user_id),
  INDEX idx_shop_id (shop_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `withdrawal_id` | BIGINT | ID duy nhất |
| `shop_id` | BIGINT | ID cửa hàng (FK) |
| `wallet_id` | BIGINT | ID ví (FK) |
| `amount` | DECIMAL(15,2) | Số tiền rút |
| `bank_account_id` | BIGINT | ID tài khoản ngân hàng |
| `bank_account_number` | VARCHAR(50) | Số tài khoản |
| `bank_name` | VARCHAR(255) | Tên ngân hàng |
| `account_name` | VARCHAR(255) | Tên chủ tài khoản |
| `status` | VARCHAR(20) | Trạng thái |
| `rejection_reason` | TEXT | Lý do từ chối |
| `processed_by` | BIGINT | Admin xử lý (FK) |
| `processed_at` | TIMESTAMP | Thời gian xử lý |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

### 21. 📊 SETTLEMENTS - Bảng Cấu Trúc Thanh Toán

```sql
CREATE TABLE settlements (
  settlement_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  settlement_date DATE NOT NULL,
  total_sales DECIMAL(15,2) DEFAULT 0,
  total_commission DECIMAL(15,2) DEFAULT 0,
  refunds DECIMAL(15,2) DEFAULT 0,
  adjustments DECIMAL(15,2) DEFAULT 0,
  net_amount DECIMAL(15,2) DEFAULT 0,
  status VARCHAR(20),               -- PENDING, SETTLED, PROCESSING
  processed_at TIMESTAMP NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (shop_id) REFERENCES shops(shop_id),
  INDEX idx_shop_id (shop_id),
  INDEX idx_settlement_date (settlement_date),
  INDEX idx_status (status),
  UNIQUE KEY uk_shop_settlement_date (shop_id, settlement_date)
);
```

**Cột:**
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| `settlement_id` | BIGINT | ID duy nhất |
| `shop_id` | BIGINT | ID cửa hàng (FK) |
| `settlement_date` | DATE | Ngày cấu trúc |
| `total_sales` | DECIMAL(15,2) | Tổng bán hàng |
| `total_commission` | DECIMAL(15,2) | Tổng hoa hồng |
| `refunds` | DECIMAL(15,2) | Hoàn tiền |
| `adjustments` | DECIMAL(15,2) | Điều chỉnh |
| `net_amount` | DECIMAL(15,2) | Số tiền ròng |
| `status` | VARCHAR(20) | Trạng thái |
| `processed_at` | TIMESTAMP | Thời gian xử lý |
| `created_at` | TIMESTAMP | Ngày tạo |
| `updated_at` | TIMESTAMP | Ngày cập nhật |

---

## 📊 Database Relationships Diagram

```
users (1) ──── (N) shops
           │
           ├──── (N) carts
           │      └──── (N) cart_items ──── (N) product_variants
           │
           ├──── (N) addresses
           │
           ├──── (N) orders ──── (N) shop_orders ──── (N) order_items ──── (N) product_variants
           │      │
           │      └──── (1) payments
           │
           ├──── (N) conversations ──── (N) chat_messages
           │
           └──── (N) refunds

shops (1) ──── (N) products ──── (N) product_variants ──── (N) order_items
         │
         ├──── (N) vouchers
         │
         ├──── (1) wallets ──── (N) wallet_transactions
         │
         ├──── (N) withdrawals
         │
         └──── (N) settlements

categories (1) ──── (N) products
```

---

## 📝 Mẫu SQL Queries Thường Dùng

### 1. Lấy tất cả sản phẩm của một shop
```sql
SELECT p.*, c.name as category_name, COUNT(oi.order_item_id) as sold_count
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
LEFT JOIN product_variants pv ON p.product_id = pv.product_id
LEFT JOIN order_items oi ON pv.variant_id = oi.variant_id
WHERE p.shop_id = ? AND p.status = 'ACTIVE'
GROUP BY p.product_id
ORDER BY p.created_at DESC;
```

### 2. Lấy tất cả đơn hàng của một shop
```sql
SELECT so.*, o.total_amount, u.full_name, u.phone, COUNT(oi.order_item_id) as item_count
FROM shop_orders so
JOIN orders o ON so.order_id = o.order_id
JOIN users u ON o.user_id = u.user_id
LEFT JOIN order_items oi ON so.shop_order_id = oi.shop_order_id
WHERE so.shop_id = ? AND so.status != 'CANCELLED'
GROUP BY so.shop_order_id
ORDER BY so.created_at DESC;
```

### 3. Tính số dư ví của một shop
```sql
SELECT 
  w.wallet_id,
  w.shop_id,
  SUM(CASE WHEN wt.type = 'ORDER_COMMISSION' THEN wt.amount ELSE 0 END) as total_commission,
  SUM(CASE WHEN wt.type = 'REFUND' THEN -wt.amount ELSE 0 END) as total_refund,
  SUM(CASE WHEN wt.type = 'WITHDRAWAL' THEN -wt.amount ELSE 0 END) as total_withdrawn
FROM wallets w
LEFT JOIN wallet_transactions wt ON w.wallet_id = wt.wallet_id
WHERE w.shop_id = ?
GROUP BY w.wallet_id;
```

### 4. Lấy danh sách chat của một user
```sql
SELECT c.*, 
  u2.full_name as partner_name, u2.avatar as partner_avatar,
  cm.message as last_message, cm.created_at as last_message_time,
  COUNT(CASE WHEN cm.is_read = 0 AND cm.receiver_id = ? THEN 1 END) as unread_count
FROM conversations c
LEFT JOIN users u2 ON (c.user_id_1 = ? AND c.user_id_2 = u2.user_id) 
                   OR (c.user_id_2 = ? AND c.user_id_1 = u2.user_id)
LEFT JOIN chat_messages cm ON c.conversation_id = cm.conversation_id
WHERE c.user_id_1 = ? OR c.user_id_2 = ?
GROUP BY c.conversation_id
ORDER BY c.last_message_at DESC;
```

---

## 🔑 Indexes Chính

| Bảng | Index | Tác dụng |
|------|-------|---------|
| `users` | `email` | Tìm kiếm nhanh khi login |
| `products` | `shop_id, status` | Lấy sản phẩm theo shop |
| `orders` | `user_id, status` | Lấy đơn hàng của user |
| `shop_orders` | `shop_id, status` | Lấy đơn hàng của shop |
| `cart_items` | `cart_id, variant_id` | Tìm nhanh item trong giỏ |
| `chat_messages` | `conversation_id, is_read` | Chat performance |
| `payments` | `order_id, status` | Tìm thanh toán nhanh |
| `wallets` | `shop_id` | Lấy ví của shop |

---

## 🔒 Database Views (Optional)

### View: order_summary (Tóm tắt đơn hàng)
```sql
CREATE VIEW order_summary AS
SELECT 
  o.order_id,
  u.full_name as buyer_name,
  s.shop_name,
  COUNT(oi.order_item_id) as item_count,
  o.total_amount,
  o.status as payment_status,
  so.status as order_status,
  o.created_at
FROM orders o
JOIN users u ON o.user_id = u.user_id
JOIN shop_orders so ON o.order_id = so.order_id
JOIN shops s ON so.shop_id = s.shop_id
LEFT JOIN order_items oi ON so.shop_order_id = oi.shop_order_id
GROUP BY o.order_id;
```

---

## 📦 Database Configuration

**File:** `src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smartcart_db?useSSL=false&serverTimezone=UTC
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        show_sql: false
        use_sql_comments: true
    open-in-view: false
    
  flyway:
    baselineOnMigrate: true
    locations: classpath:db/migration
```

Tài liệu này mô tả chi tiết cấu trúc cơ sở dữ liệu SmartCart bao gồm:
✅ Tất cả bảng chính với cột chi tiết
✅ Relationship giữa các bảng
✅ Primary key & Foreign key
✅ Indexes để tối ưu hóa
✅ Mẫu SQL queries
✅ Database configuration
