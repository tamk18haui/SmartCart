package com.gr6.SmartCart.module_v3.withdraw.service.impl;

import com.gr6.SmartCart.common.base.BaseResponse;
import com.gr6.SmartCart.common.base.PageResponse;
import com.gr6.SmartCart.common.domain.*;
import com.gr6.SmartCart.common.enums.*;
import com.gr6.SmartCart.module_v3.withdraw.dto.*;
import com.gr6.SmartCart.module_v3.withdraw.repository.*;
import com.gr6.SmartCart.module_v3.withdraw.service.AdminWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminWithdrawServiceImpl implements AdminWithdrawService {

    private static final int PLATFORM_COMMISSION_PERCENT = 0;

    private final WithdrawShopOrderRepository shopOrderRepository;
    private final SellerSettlementRepository settlementRepository;
    private final WithdrawWalletRepository walletRepository;
    private final WithdrawWalletTransactionRepository walletTransactionRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;

    @Override
    @Transactional
    public BaseResponse<ReconcileResponse> reconcileCompletedOrders() {
        String adminEmail = currentEmail();

        List<ShopOrder> shopOrders = shopOrderRepository.findCompletedUnsettledShopOrders();

        List<SellerSettlementResponse> settlements = new ArrayList<>();

        long totalGross = 0L;
        long totalCommission = 0L;
        long totalNet = 0L;

        for (ShopOrder shopOrder : shopOrders) {
            if (settlementRepository.existsByShopOrder_ShopOrderId(shopOrder.getShopOrderId())) {
                continue;
            }

            Shop shop = shopOrder.getShop();
            User seller = shop.getUser();

            Wallet wallet = getOrCreateWallet(seller);
            Wallet lockedWallet = walletRepository.findByUserIdForUpdate(seller.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví seller"));

            long gross = shopOrder.getTotalAmount() == null ? 0L : shopOrder.getTotalAmount();
            long commission = gross * PLATFORM_COMMISSION_PERCENT / 100;
            long net = gross - commission;

            lockedWallet.setBalance((lockedWallet.getBalance() == null ? 0L : lockedWallet.getBalance()) + net);
            walletRepository.save(lockedWallet);

            SellerSettlement settlement = SellerSettlement.builder()
                    .shopOrder(shopOrder)
                    .seller(seller)
                    .shop(shop)
                    .grossAmount(gross)
                    .commissionAmount(commission)
                    .netAmount(net)
                    .status(SettlementStatus.SETTLED)
                    .note("Đối soát shopOrder #" + shopOrder.getShopOrderId())
                    .settledBy(adminEmail)
                    .build();

            settlement = settlementRepository.save(settlement);

            WalletTransaction tx = new WalletTransaction();
            tx.setWallet(lockedWallet);
            tx.setType(WalletTransactionType.TOP_UP);
            tx.setAmount(net);
            tx.setDescription("Đối soát shopOrder #" + shopOrder.getShopOrderId());
            walletTransactionRepository.save(tx);

            totalGross += gross;
            totalCommission += commission;
            totalNet += net;

            settlements.add(SellerSettlementResponse.from(settlement));
        }

        ReconcileResponse response = ReconcileResponse.builder()
                .settledCount(settlements.size())
                .totalGrossAmount(totalGross)
                .totalCommissionAmount(totalCommission)
                .totalNetAmount(totalNet)
                .settlements(settlements)
                .build();

        return BaseResponse.success_data("Đối soát thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<SellerSettlementResponse>> getSettlements(int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "settlementId")
        );

        Page<SellerSettlementResponse> responsePage =
                settlementRepository
                        .findAllByOrderBySettlementIdDesc(pageable)
                        .map(SellerSettlementResponse::from);

        return BaseResponse.success_data(
                "Lấy danh sách đối soát thành công",
                PageResponse.of(responsePage)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<WithdrawResponse>> getWithdrawRequests(
            WithdrawStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "withdrawId")
        );

        Page<WithdrawRequest> requests = status == null
                ? withdrawRequestRepository.findAllByOrderByWithdrawIdDesc(pageable)
                : withdrawRequestRepository.findByStatusOrderByWithdrawIdDesc(status, pageable);

        Page<WithdrawResponse> responsePage = requests.map(WithdrawResponse::from);

        return BaseResponse.success_data(
                "Lấy danh sách yêu cầu rút tiền thành công",
                PageResponse.of(responsePage)
        );
    }

    @Override
    @Transactional
    public BaseResponse<WithdrawResponse> approveWithdraw(
            Long withdrawId,
            AdminWithdrawDecisionRequest request
    ) {
        WithdrawRequest withdraw = withdrawRequestRepository.findByIdForUpdate(withdrawId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu rút tiền"));

        if (withdraw.getStatus() != WithdrawStatus.PENDING) {
            throw new RuntimeException("Yêu cầu rút tiền đã được xử lý");
        }

        withdraw.setStatus(WithdrawStatus.APPROVED);
        withdraw.setAdminNote(request == null ? null : request.getAdminNote());
        withdraw.setTransferCode(request == null ? null : request.getTransferCode());
        withdraw.setProcessedBy(currentEmail());
        withdraw.setProcessedAt(LocalDateTime.now());

        withdraw = withdrawRequestRepository.save(withdraw);

        return BaseResponse.success_data(
                "Duyệt rút tiền thành công",
                WithdrawResponse.from(withdraw)
        );
    }

    @Override
    @Transactional
    public BaseResponse<WithdrawResponse> rejectWithdraw(
            Long withdrawId,
            AdminWithdrawDecisionRequest request
    ) {
        WithdrawRequest withdraw = withdrawRequestRepository.findByIdForUpdate(withdrawId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu rút tiền"));

        if (withdraw.getStatus() != WithdrawStatus.PENDING) {
            throw new RuntimeException("Yêu cầu rút tiền đã được xử lý");
        }

        Wallet wallet = walletRepository.findByUserIdForUpdate(withdraw.getSeller().getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví seller"));

        wallet.setBalance((wallet.getBalance() == null ? 0L : wallet.getBalance()) + withdraw.getAmount());
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setType(WalletTransactionType.TOP_UP);
        tx.setAmount(withdraw.getAmount());
        tx.setDescription("Hoàn tiền do admin từ chối yêu cầu rút #" + withdraw.getWithdrawId());
        walletTransactionRepository.save(tx);

        withdraw.setStatus(WithdrawStatus.REJECTED);
        withdraw.setAdminNote(request == null ? null : request.getAdminNote());
        withdraw.setProcessedBy(currentEmail());
        withdraw.setProcessedAt(LocalDateTime.now());

        withdraw = withdrawRequestRepository.save(withdraw);

        return BaseResponse.success_data(
                "Từ chối rút tiền thành công, tiền đã hoàn lại ví seller",
                WithdrawResponse.from(withdraw)
        );
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

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}