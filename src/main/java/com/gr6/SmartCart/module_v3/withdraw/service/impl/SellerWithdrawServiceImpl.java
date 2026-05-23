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

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerWithdrawServiceImpl implements SellerWithdrawService {

    private static final int PLATFORM_COMMISSION_PERCENT = 0;

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final WithdrawWalletRepository walletRepository;
    private final WithdrawWalletTransactionRepository walletTransactionRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final SellerSettlementRepository settlementRepository;
    private final WithdrawShopOrderRepository shopOrderRepository;

    @Override
    @Transactional
    public BaseResponse<WalletSummaryResponse> getMyWallet() {
        User seller = getCurrentSeller();
        Shop shop = getCurrentShop(seller);
        Wallet wallet = getOrCreateWallet(seller);

        settleSellerPayableOrders(shop, seller);

        wallet = walletRepository.findByUser_UserId(seller.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví seller"));

        return BaseResponse.success_data(
                "Lấy ví seller thành công",
                WalletSummaryResponse.from(wallet, shop)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<WalletTransactionResponse>> getMyWalletTransactions(int page, int size) {
        User seller = getCurrentSeller();

        Wallet wallet = walletRepository.findByUser_UserId(seller.getUserId())
                .orElseThrow(() -> new RuntimeException("Seller chưa có ví"));

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "walletTxId")
        );

        Page<WalletTransactionResponse> responsePage =
                walletTransactionRepository
                        .findByWallet_WalletIdOrderByWalletTxIdDesc(wallet.getWalletId(), pageable)
                        .map(WalletTransactionResponse::from);

        return BaseResponse.success_data(
                "Lấy lịch sử ví thành công",
                PageResponse.of(responsePage)
        );
    }

    @Override
    @Transactional
    public BaseResponse<WithdrawResponse> createWithdrawRequest(WithdrawCreateRequest request) {
        User seller = getCurrentSeller();
        Shop shop = getCurrentShop(seller);

        getOrCreateWallet(seller);
        settleSellerPayableOrders(shop, seller);

        Wallet lockedWallet = walletRepository.findByUserIdForUpdate(seller.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví seller"));

        if (lockedWallet.getStatus() == WalletStatus.LOCKED) {
            throw new RuntimeException("Ví seller đang bị khóa");
        }

        Long amount = request.getAmount();
        Long currentBalance = lockedWallet.getBalance() == null ? 0L : lockedWallet.getBalance();

        if (amount == null || amount < 10000) {
            throw new RuntimeException("Số tiền rút tối thiểu là 10.000đ");
        }

        if (currentBalance < amount) {
            throw new RuntimeException("Số dư ví không đủ");
        }

        lockedWallet.setBalance(currentBalance - amount);
        walletRepository.save(lockedWallet);

        WithdrawRequest withdraw = WithdrawRequest.builder()
                .wallet(lockedWallet)
                .seller(seller)
                .shop(shop)
                .amount(amount)
                .bankName(request.getBankName().trim())
                .bankAccountNumber(request.getBankAccountNumber().trim())
                .bankAccountHolder(request.getBankAccountHolder().trim())
                .sellerNote(request.getSellerNote())
                .status(WithdrawStatus.PENDING)
                .build();

        withdraw = withdrawRequestRepository.save(withdraw);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(lockedWallet);
        tx.setType(WalletTransactionType.WITHDRAW);
        tx.setAmount(amount);
        tx.setDescription("Seller tạo yêu cầu rút tiền #" + withdraw.getWithdrawId());
        walletTransactionRepository.save(tx);

        return BaseResponse.success_data(
                "Tạo yêu cầu rút tiền thành công",
                WithdrawResponse.from(withdraw)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<WithdrawResponse>> getMyWithdrawRequests(int page, int size) {
        String email = currentEmail();

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "withdrawId")
        );

        Page<WithdrawResponse> responsePage =
                withdrawRequestRepository
                        .findBySeller_EmailOrderByWithdrawIdDesc(email, pageable)
                        .map(WithdrawResponse::from);

        return BaseResponse.success_data(
                "Lấy danh sách yêu cầu rút tiền thành công",
                PageResponse.of(responsePage)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<SellerSettlementResponse>> getMySettlements(int page, int size) {
        String email = currentEmail();

        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "settlementId")
        );

        Page<SellerSettlementResponse> responsePage =
                settlementRepository
                        .findBySeller_EmailOrderBySettlementIdDesc(email, pageable)
                        .map(SellerSettlementResponse::from);

        return BaseResponse.success_data(
                "Lấy lịch sử đối soát thành công",
                PageResponse.of(responsePage)
        );
    }

    private void settleSellerPayableOrders(Shop shop, User seller) {
        List<ShopOrder> orders = shopOrderRepository.findSellerPayableUnsettledShopOrders(shop.getShopId());
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Wallet lockedWallet = walletRepository.findByUserIdForUpdate(seller.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví seller"));

        if (lockedWallet.getStatus() == WalletStatus.LOCKED) {
            throw new RuntimeException("Ví seller đang bị khóa");
        }

        long currentBalance = lockedWallet.getBalance() == null ? 0L : lockedWallet.getBalance();

        for (ShopOrder shopOrder : orders) {
            if (shopOrder == null || shopOrder.getShopOrderId() == null) {
                continue;
            }

            if (settlementRepository.existsByShopOrder_ShopOrderId(shopOrder.getShopOrderId())) {
                continue;
            }

            long gross = shopOrder.getTotalAmount() == null ? 0L : shopOrder.getTotalAmount();
            long commission = gross * PLATFORM_COMMISSION_PERCENT / 100;
            long net = gross - commission;

            currentBalance += net;

            SellerSettlement settlement = SellerSettlement.builder()
                    .shopOrder(shopOrder)
                    .seller(seller)
                    .shop(shop)
                    .grossAmount(gross)
                    .commissionAmount(commission)
                    .netAmount(net)
                    .status(SettlementStatus.SETTLED)
                    .note("Tự động cộng ví seller khi đơn đã giao/hoàn thành")
                    .settledBy("SYSTEM")
                    .build();

            settlementRepository.save(settlement);

            WalletTransaction tx = new WalletTransaction();
            tx.setWallet(lockedWallet);
            tx.setType(WalletTransactionType.TOP_UP);
            tx.setAmount(net);
            tx.setDescription("Thanh toán đơn #" + shopOrder.getShopOrderId());
            walletTransactionRepository.save(tx);
        }

        lockedWallet.setBalance(currentBalance);
        walletRepository.save(lockedWallet);
    }

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

    private User getCurrentSeller() {
        String email = currentEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));

        if (user.getRole() != UserRole.SELLER) {
            throw new RuntimeException("Chỉ seller mới được sử dụng chức năng này");
        }

        return user;
    }

    private Shop getCurrentShop(User seller) {
        Shop shop = shopRepository.findByUser_Email(seller.getEmail())
                .orElseThrow(() -> new RuntimeException("Seller chưa có shop"));

        if (shop.getStatus() != ShopStatus.ACTIVE) {
            throw new RuntimeException("Shop chưa hoạt động hoặc đang bị khóa");
        }

        return shop;
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
