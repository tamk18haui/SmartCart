*Các bạn clone về và tạo 1 file .env (cùng cấp với pom.xml) bên trong bao gồm
DB_URL=jdbc:mysql://localhost:3306/smartcart_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USER=root
DB_PASS= mật khẩu của bạn
CLOUD_NAME=dtnyw0cyr
CLOUD_KEY=995576575431699
CLOUD_SECRET=6i9QW__zcJjDm-G049BRjYwYlqc
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION=86400000

các bạn nhớ tạo trong mysql 1 database có tên "smartcart_db" nhé

*Đây là cây thư mục của sprint 1 này vì đẩy code lên git package rỗng không được
nên các bạn tự tạo package phần của mình nhé!
smartcart-backend
├── pom.xml                                   # Nơi Tâm cài đặt thư viện (Spring Web, JPA, MySQL, JWT...)
├── src/main/resources
│   └── application.yml                       # Tâm cấu hình cổng kết nối (Port 8080) và Database
│
└── src/main/java/com/smartcart
├── SmartCartApplication.java             # File Main chạy hàm public static void main()
│
├── common                                # 🛑 VÙNG LÕI TÀI NGUYÊN (TÂM QUẢN LÝ)
│   ├── domain                            # Chứa 100% các class @Entity cho 5 Cụm Bảng Database
│   ├── base                              # Các class phản hồi chung: BaseResponse, PageResponse
│   ├── exception                         # GlobalExceptionHandler (Bắt lỗi toàn hệ thống)
│   └── security                          # 🛡️ Tâm code bộ lọc an ninh (SMAR-13)
│       ├── JwtTokenProvider.java         # Class chuyên tạo và xác thực chuỗi token
│       ├── JwtAuthenticationFilter.java  # Màng lọc chặn các Request không có thẻ (token)
│       └── SecurityConfig.java           # Phân quyền: Mở API Đăng nhập, Khóa API của Seller
│
└── modules                               # 🚀 5 LÃNH THỔ TÁC CHIẾN SPRINT 1
│
├── identity                          # 👤 ĐẢO HƯỞNG (Auth & Hồ sơ)
│   ├── controller
│   │   ├── AuthController.java       # (SMAR-5, 11) API Nhận email/pass -> Đăng ký, Đăng nhập
│   │   └── ShopProfileController.java# (SMAR-8, 50) API Đăng ký mở shop, Sửa tên/logo shop
│   ├── service
│   │   └── AuthService.java          # Logic check mật khẩu, gọi JwtTokenProvider để in thẻ
│   └── dto                           # LoginRequest, RegisterShopRequest
│
├── catalog                           # 📦 ĐẢO SÁNG (Hàng hóa & Kho bãi)
│   ├── controller
│   │   ├── ProductController.java    # (SMAR-53) API Seller Thêm/Sửa/Xóa sản phẩm
│   │   ├── CategoryController.java   # (SMAR-69) API Quản lý danh mục
│   │   └── InventoryController.java  # (SMAR-56) API Cập nhật số lượng tồn kho
│   ├── service
│   │   └── InventoryService.java     # Chứa hàm decreaseStock() để Tâm gọi trừ kho
│   └── dto                           # ProductRequest, ProductResponse
│
├── storefront                        # 🏪 ĐẢO TOÀN (Giao diện mua sắm Buyer)
│   ├── controller
│   │   └── DiscoveryController.java  # (SMAR-23, 26) API Lấy list sản phẩm ra trang chủ & Tìm kiếm
│   ├── service
│   │   └── DiscoveryService.java     # Chứa logic lọc giá, tìm theo tên
│   └── dto                           # SearchFilterRequest
│
├── fulfillment                       # 🛒 ĐẢO HUY (Hậu cần & Vận hành đơn)
│   ├── controller
│   │   ├── MatrixController.java     # (SMAR-104) API check giá khi khách đổi Màu/Size
│   │   └── ShopOrderController.java  # (SMAR-59) API Seller xem list đơn, bấm Duyệt/Giao hàng
│   ├── service
│   │   └── FulfillmentService.java   # Chứa hàm nhận Đơn tổng từ Tâm để xé lẻ ra cho từng Shop
│   └── dto                           # OrderStatusUpdateDTO
│
└── finance_core                      # 💰 ĐẢO TÂM (Chốt đơn thanh toán)
├── controller
│   └── CheckoutController.java   # (SMAR-38) API Bấm Đặt Hàng (Mặc định COD)
├── service
│   └── CheckoutService.java      # Gọi Sáng trừ kho -> Lưu bảng Orders -> Gọi Huy xé đơn
└── dto                           # CheckoutRequest (Gửi kèm list hàng & địa chỉ)
