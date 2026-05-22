package com.gr6.SmartCart.module_v3.withdraw.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.common.enums.*;
import com.gr6.SmartCart.module_v3.withdraw.dto.*;

import com.gr6.SmartCart.module_v3.withdraw.repository.*;
import com.gr6.SmartCart.module_v3.withdraw.service.SellerWithdrawService;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerWithdrawServiceImpl implements SellerWithdrawService {

    // Repository dùng để tìm thông tin user hiện tại
    private final UserRepository userRepository;

    // Repository dùng để tìm shop của seller hiện tại
    private final ShopRepository shopRepository;

    // Repository quản lý ví của seller
    private final WithdrawWalletRepository walletRepository;

    // Repository lưu và truy vấn lịch sử giao dịch ví
    private final WithdrawWalletTransactionRepository walletTransactionRepository;

    // Repository quản lý yêu cầu rút tiền của seller
    private final WithdrawRequestRepository withdrawRequestRepository;

    // Repository quản lý lịch sử đối soát của seller
    private final SellerSettlementRepository settlementRepository;

    /**
     * Chức năng: Seller xem thông tin ví của mình.
     *
     * Luồng xử lý:
     * 1. Lấy seller hiện tại từ tài khoản đang đăng nhập.
     * 2. Lấy shop của seller.
     * 3. Lấy ví của seller, nếu chưa có thì tự tạo ví mới.
     * 4. Trả về thông tin ví kèm thông tin shop.
     */
    @Override
    @Transactional
    public BaseResponse<WalletSummaryResponse> getMyWallet() {
        // Lấy seller hiện tại đang đăng nhập
        User seller = getCurrentSeller();

        // Lấy shop của seller, đồng thời kiểm tra shop có hoạt động không
        Shop shop = getCurrentShop(seller);

        // Lấy ví của seller, nếu chưa có ví thì tạo mới
        Wallet wallet = getOrCreateWallet(seller);

        // Trả về thông tin ví cho client
        return BaseResponse.success_data(
                "Lấy ví seller thành công",
                WalletSummaryResponse.from(wallet, shop)
        );
    }

    /**
     * Chức năng: Seller xem lịch sử giao dịch ví.
     *
     * Ví dụ lịch sử gồm:
     * - Tiền được cộng vào ví sau khi admin đối soát đơn hàng.
     * - Tiền bị trừ khi seller tạo yêu cầu rút tiền.
     * - Tiền được hoàn lại nếu admin từ chối yêu cầu rút tiền.
     */
    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<WalletTransactionResponse>> getMyWalletTransactions(int page, int size) {
        // Lấy seller hiện tại
        User seller = getCurrentSeller();

        // Tìm ví của seller
        // Nếu seller chưa có ví thì báo lỗi
        Wallet wallet = walletRepository.findByUser_UserId(seller.getUserId())
                .orElseThrow(() -> new RuntimeException("Seller chưa có ví"));

        // Tạo thông tin phân trang
        // page - 1 vì Spring Data JPA bắt đầu từ trang 0
        // Math.max để tránh page hoặc size truyền vào bị âm hoặc bằng 0
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "walletTxId")
        );

        // Lấy lịch sử giao dịch của ví, sắp xếp giao dịch mới nhất lên đầu
        Page<WalletTransactionResponse> responsePage =
                walletTransactionRepository
                        .findByWallet_WalletIdOrderByWalletTxIdDesc(wallet.getWalletId(), pageable)
                        .map(WalletTransactionResponse::from);

        // Trả về danh sách lịch sử giao dịch ví
        return BaseResponse.success_data(
                "Lấy lịch sử ví thành công",
                PageResponse.of(responsePage)
        );
    }

    /**
     * Chức năng: Seller tạo yêu cầu rút tiền.
     *
     * Luồng xử lý:
     * 1. Lấy seller hiện tại.
     * 2. Lấy shop của seller.
     * 3. Lấy ví seller, nếu chưa có thì tạo mới.
     * 4. Khóa ví bằng findByUserIdForUpdate để tránh lỗi xử lý đồng thời.
     * 5. Kiểm tra ví có bị khóa không.
     * 6. Kiểm tra số tiền rút tối thiểu.
     * 7. Kiểm tra số dư có đủ không.
     * 8. Trừ tiền trong ví seller.
     * 9. Tạo yêu cầu rút tiền trạng thái PENDING.
     * 10. Tạo lịch sử giao dịch ví loại WITHDRAW.
     */
    @Override
    @Transactional
    public BaseResponse<WithdrawResponse> createWithdrawRequest(WithdrawCreateRequest request) {
        // Lấy seller hiện tại đang đăng nhập
        User seller = getCurrentSeller();

        // Lấy shop của seller và kiểm tra shop có đang ACTIVE không
        Shop shop = getCurrentShop(seller);

        // Đảm bảo seller có ví
        // Nếu chưa có thì tạo ví mới với số dư 0
        getOrCreateWallet(seller);

        // Lấy lại ví và khóa bản ghi ví để tránh nhiều request rút tiền cùng lúc
        Wallet lockedWallet = walletRepository.findByUserIdForUpdate(seller.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví seller"));

        // Nếu ví đang bị khóa thì không cho rút tiền
        if (lockedWallet.getStatus() == WalletStatus.LOCKED) {
            throw new RuntimeException("Ví seller đang bị khóa");
        }

        // Lấy số tiền seller muốn rút
        Long amount = request.getAmount();

        // Lấy số dư hiện tại của ví
        // Nếu balance null thì coi như bằng 0
        Long currentBalance = lockedWallet.getBalance() == null ? 0L : lockedWallet.getBalance();

        // Kiểm tra số tiền rút hợp lệ
        // Yêu cầu rút tối thiểu là 10.000đ
        if (amount == null || amount < 10000) {
            throw new RuntimeException("Số tiền rút tối thiểu là 10.000đ");
        }

        // Kiểm tra số dư ví có đủ để rút không
        if (currentBalance < amount) {
            throw new RuntimeException("Số dư ví không đủ");
        }

        // Trừ tiền trong ví ngay khi seller tạo yêu cầu rút tiền
        // Nếu admin từ chối thì tiền sẽ được hoàn lại ở service admin
        lockedWallet.setBalance(currentBalance - amount);

        // Lưu ví sau khi trừ tiền
        walletRepository.save(lockedWallet);

        // Tạo yêu cầu rút tiền mới
        WithdrawRequest withdraw = WithdrawRequest.builder()
                .wallet(lockedWallet)
                .seller(seller)
                .shop(shop)
                .amount(amount)

                // Thông tin ngân hàng nhận tiền
                .bankName(request.getBankName().trim())
                .bankAccountNumber(request.getBankAccountNumber().trim())
                .bankAccountHolder(request.getBankAccountHolder().trim())

                // Ghi chú của seller nếu có
                .sellerNote(request.getSellerNote())

                // Trạng thái ban đầu là chờ admin xử lý
                .status(WithdrawStatus.PENDING)
                .build();

        // Lưu yêu cầu rút tiền vào database
        withdraw = withdrawRequestRepository.save(withdraw);

        // Tạo lịch sử giao dịch ví loại WITHDRAW
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(lockedWallet);
        tx.setType(WalletTransactionType.WITHDRAW);
        tx.setAmount(amount);
        tx.setDescription("Seller tạo yêu cầu rút tiền #" + withdraw.getWithdrawId());

        // Lưu lịch sử giao dịch ví
        walletTransactionRepository.save(tx);

        // Trả về thông tin yêu cầu rút tiền vừa tạo
        return BaseResponse.success_data(
                "Tạo yêu cầu rút tiền thành công",
                WithdrawResponse.from(withdraw)
        );
    }

    /**
     * Chức năng: Seller xem danh sách yêu cầu rút tiền của mình.
     *
     * Danh sách này có thể gồm các trạng thái:
     * - PENDING: đang chờ admin xử lý
     * - APPROVED: admin đã duyệt
     * - REJECTED: admin đã từ chối
     */
    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<WithdrawResponse>> getMyWithdrawRequests(int page, int size) {
        // Lấy email tài khoản đang đăng nhập
        String email = currentEmail();

        // Tạo thông tin phân trang
        // Sắp xếp yêu cầu rút tiền mới nhất lên đầu
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "withdrawId")
        );

        // Tìm các yêu cầu rút tiền theo email seller
        Page<WithdrawResponse> responsePage =
                withdrawRequestRepository
                        .findBySeller_EmailOrderByWithdrawIdDesc(email, pageable)
                        .map(WithdrawResponse::from);

        // Trả về danh sách yêu cầu rút tiền
        return BaseResponse.success_data(
                "Lấy danh sách yêu cầu rút tiền thành công",
                PageResponse.of(responsePage)
        );
    }

    /**
     * Chức năng: Seller xem lịch sử đối soát của mình.
     *
     * Đối soát là quá trình admin cộng tiền đơn hàng đã hoàn thành vào ví seller.
     * Mỗi bản ghi đối soát thường gắn với một shopOrder.
     */
    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<SellerSettlementResponse>> getMySettlements(int page, int size) {
        // Lấy email seller đang đăng nhập
        String email = currentEmail();

        // Tạo thông tin phân trang
        // Sắp xếp bản ghi đối soát mới nhất lên đầu
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "settlementId")
        );

        // Lấy danh sách đối soát của seller theo email
        Page<SellerSettlementResponse> responsePage =
                settlementRepository
                        .findBySeller_EmailOrderBySettlementIdDesc(email, pageable)
                        .map(SellerSettlementResponse::from);

        // Trả về lịch sử đối soát
        return BaseResponse.success_data(
                "Lấy lịch sử đối soát thành công",
                PageResponse.of(responsePage)
        );
    }

    /**
     * Chức năng: Lấy ví của seller.
     *
     * Nếu seller chưa có ví thì tự động tạo ví mới:
     * - user = seller
     * - balance = 0
     * - status = ACTIVE
     */
    private Wallet getOrCreateWallet(User seller) {
        return walletRepository.findByUser_UserId(seller.getUserId())
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setUser(seller);
                    wallet.setBalance(0L);
                    wallet.setStatus(WalletStatus.ACTIVE);
                    return walletRepository.save(wallet);
                });
    }

    /**
     * Chức năng: Lấy thông tin seller hiện tại.
     *
     * Luồng xử lý:
     * 1. Lấy email từ SecurityContext.
     * 2. Tìm user theo email.
     * 3. Kiểm tra user có role SELLER không.
     *
     * Nếu không phải SELLER thì không cho dùng chức năng ví/rút tiền.
     */
    private User getCurrentSeller() {
        // Lấy email người dùng đang đăng nhập
        String email = currentEmail();

        // Tìm user theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));

        // Kiểm tra role, chỉ seller mới được sử dụng chức năng này
        if (user.getRole() != UserRole.SELLER) {
            throw new RuntimeException("Chỉ seller mới được sử dụng chức năng này");
        }

        return user;
    }

    /**
     * Chức năng: Lấy shop của seller hiện tại.
     *
     * Điều kiện:
     * - Seller phải có shop.
     * - Shop phải ở trạng thái ACTIVE.
     */
    private Shop getCurrentShop(User seller) {
        // Tìm shop theo email của seller
        Shop shop = shopRepository.findByUser_Email(seller.getEmail())
                .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));

        // Nếu shop chưa hoạt động hoặc bị khóa thì không cho thao tác ví/rút tiền
        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop chưa hoạt động hoặc đang bị khóa");
        }

        return shop;
    }

    /**
     * Chức năng: Lấy email hoặc username của tài khoản đang đăng nhập.
     *
     * Dữ liệu được lấy từ Spring SecurityContext.
     */
    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}