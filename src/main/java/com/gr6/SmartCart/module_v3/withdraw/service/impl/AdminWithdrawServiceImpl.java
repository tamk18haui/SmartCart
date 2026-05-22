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

    // Phần trăm hoa hồng của sàn.
    // Hiện tại để 0%, nghĩa là seller nhận toàn bộ tiền của đơn hàng.
    private static final int PLATFORM_COMMISSION_PERCENT = 0;

    // Repository dùng để lấy các đơn hàng theo từng shop.
    private final WithdrawShopOrderRepository shopOrderRepository;

    // Repository dùng để lưu và truy vấn lịch sử đối soát.
    private final SellerSettlementRepository settlementRepository;

    // Repository quản lý ví của seller.
    private final WithdrawWalletRepository walletRepository;

    // Repository lưu lịch sử giao dịch ví.
    private final WithdrawWalletTransactionRepository walletTransactionRepository;

    // Repository quản lý yêu cầu rút tiền của seller.
    private final WithdrawRequestRepository withdrawRequestRepository;

    /**
     * Chức năng: Admin đối soát các đơn hàng đã hoàn thành.
     *
     * Luồng xử lý:
     * 1. Lấy danh sách shopOrder đã hoàn thành nhưng chưa đối soát.
     * 2. Với mỗi shopOrder, kiểm tra tránh đối soát trùng.
     * 3. Lấy seller của shop.
     * 4. Tạo ví seller nếu chưa có.
     * 5. Khóa ví seller để tránh lỗi cộng tiền đồng thời.
     * 6. Tính tổng tiền, hoa hồng, tiền thực nhận.
     * 7. Cộng tiền thực nhận vào ví seller.
     * 8. Tạo bản ghi đối soát.
     * 9. Tạo lịch sử giao dịch ví.
     * 10. Trả về kết quả đối soát.
     */
    @Override
    @Transactional
    public BaseResponse<ReconcileResponse> reconcileCompletedOrders() {
        // Lấy email admin đang đăng nhập để lưu lại người thực hiện đối soát.
        String adminEmail = currentEmail();

        // Lấy danh sách các đơn shop đã hoàn thành nhưng chưa được đối soát.
        List<ShopOrder> shopOrders = shopOrderRepository.findCompletedUnsettledShopOrders();

        // Danh sách kết quả đối soát trả về cho client.
        List<SellerSettlementResponse> settlements = new ArrayList<>();

        // Các biến thống kê tổng tiền.
        long totalGross = 0L;
        long totalCommission = 0L;
        long totalNet = 0L;

        // Duyệt từng đơn hàng của shop.
        for (ShopOrder shopOrder : shopOrders) {

            // Kiểm tra nếu shopOrder này đã có bản ghi đối soát thì bỏ qua.
            // Điều này giúp tránh cộng tiền 2 lần cho cùng một đơn hàng.
            if (settlementRepository.existsByShopOrder_ShopOrderId(shopOrder.getShopOrderId())) {
                continue;
            }

            // Lấy shop từ đơn hàng.
            Shop shop = shopOrder.getShop();

            // Lấy seller là user sở hữu shop.
            User seller = shop.getUser();

            // Tạo ví nếu seller chưa có ví.
            getOrCreateWallet(seller);

            // Lấy lại ví và khóa bản ghi ví để tránh nhiều giao dịch cùng lúc làm sai số dư.
            Wallet lockedWallet = walletRepository.findByUserIdForUpdate(seller.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví seller"));

            // Tổng tiền đơn hàng của shop.
            // Nếu totalAmount null thì coi như bằng 0.
            long gross = shopOrder.getTotalAmount() == null ? 0L : shopOrder.getTotalAmount();

            // Tính tiền hoa hồng của sàn.
            long commission = gross * PLATFORM_COMMISSION_PERCENT / 100;

            // Tiền thực nhận của seller sau khi trừ hoa hồng.
            long net = gross - commission;

            // Cộng tiền thực nhận vào ví seller.
            lockedWallet.setBalance(
                    (lockedWallet.getBalance() == null ? 0L : lockedWallet.getBalance()) + net
            );

            // Lưu lại ví sau khi cập nhật số dư.
            walletRepository.save(lockedWallet);

            // Tạo bản ghi đối soát cho shopOrder.
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

            // Lưu bản ghi đối soát vào database.
            settlement = settlementRepository.save(settlement);

            // Tạo lịch sử giao dịch ví.
            WalletTransaction tx = new WalletTransaction();
            tx.setWallet(lockedWallet);
            tx.setType(WalletTransactionType.TOP_UP);
            tx.setAmount(net);
            tx.setDescription("Đối soát shopOrder #" + shopOrder.getShopOrderId());

            // Lưu lịch sử giao dịch ví.
            walletTransactionRepository.save(tx);

            // Cộng dồn số liệu thống kê.
            totalGross += gross;
            totalCommission += commission;
            totalNet += net;

            // Thêm kết quả đối soát vào danh sách trả về.
            settlements.add(SellerSettlementResponse.from(settlement));
        }

        // Tạo response tổng hợp kết quả đối soát.
        ReconcileResponse response = ReconcileResponse.builder()
                .settledCount(settlements.size())
                .totalGrossAmount(totalGross)
                .totalCommissionAmount(totalCommission)
                .totalNetAmount(totalNet)
                .settlements(settlements)
                .build();

        return BaseResponse.success_data("Đối soát thành công", response);
    }

    /**
     * Chức năng: Lấy danh sách lịch sử đối soát.
     *
     * Có phân trang và sắp xếp bản ghi mới nhất lên đầu.
     */
    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<SellerSettlementResponse>> getSettlements(int page, int size) {

        // Tạo cấu hình phân trang.
        // page - 1 vì Spring Data JPA bắt đầu từ page 0.
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "settlementId")
        );

        // Lấy danh sách đối soát và map từ entity sang response DTO.
        Page<SellerSettlementResponse> responsePage =
                settlementRepository
                        .findAllByOrderBySettlementIdDesc(pageable)
                        .map(SellerSettlementResponse::from);

        return BaseResponse.success_data(
                "Lấy danh sách đối soát thành công",
                PageResponse.of(responsePage)
        );
    }

    /**
     * Chức năng: Admin lấy danh sách yêu cầu rút tiền của seller.
     *
     * Nếu status null thì lấy tất cả.
     * Nếu có status thì lọc theo trạng thái:
     * - PENDING
     * - APPROVED
     * - REJECTED
     */
    @Override
    @Transactional(readOnly = true)
    public BaseResponse<PageResponse<WithdrawResponse>> getWithdrawRequests(
            WithdrawStatus status,
            int page,
            int size
    ) {

        // Tạo cấu hình phân trang, sắp xếp yêu cầu mới nhất lên đầu.
        Pageable pageable = PageRequest.of(
                Math.max(page - 1, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "withdrawId")
        );

        // Nếu không truyền status thì lấy tất cả yêu cầu rút tiền.
        // Nếu có status thì lấy theo trạng thái được truyền vào.
        Page<WithdrawRequest> requests = status == null
                ? withdrawRequestRepository.findAllByOrderByWithdrawIdDesc(pageable)
                : withdrawRequestRepository.findByStatusOrderByWithdrawIdDesc(status, pageable);

        // Chuyển từ entity sang response DTO.
        Page<WithdrawResponse> responsePage = requests.map(WithdrawResponse::from);

        return BaseResponse.success_data(
                "Lấy danh sách yêu cầu rút tiền thành công",
                PageResponse.of(responsePage)
        );
    }

    /**
     * Chức năng: Admin duyệt yêu cầu rút tiền.
     *
     * Lưu ý:
     * Hàm này không trừ tiền ví seller.
     * Điều đó có nghĩa là tiền đã được trừ hoặc giữ lại từ lúc seller tạo yêu cầu rút tiền.
     */
    @Override
    @Transactional
    public BaseResponse<WithdrawResponse> approveWithdraw(
            Long withdrawId,
            AdminWithdrawDecisionRequest request
    ) {

        // Tìm yêu cầu rút tiền theo ID và khóa bản ghi để tránh xử lý đồng thời.
        WithdrawRequest withdraw = withdrawRequestRepository.findByIdForUpdate(withdrawId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu rút tiền"));

        // Chỉ được duyệt yêu cầu đang ở trạng thái PENDING.
        if (withdraw.getStatus() != WithdrawStatus.PENDING) {
            throw new RuntimeException("Yêu cầu rút tiền đã được xử lý");
        }

        // Cập nhật trạng thái sang APPROVED.
        withdraw.setStatus(WithdrawStatus.APPROVED);

        // Lưu ghi chú của admin nếu có.
        withdraw.setAdminNote(request == null ? null : request.getAdminNote());

        // Lưu mã chuyển khoản nếu admin nhập.
        withdraw.setTransferCode(request == null ? null : request.getTransferCode());

        // Lưu người xử lý là admin hiện tại.
        withdraw.setProcessedBy(currentEmail());

        // Lưu thời gian xử lý.
        withdraw.setProcessedAt(LocalDateTime.now());

        // Lưu lại yêu cầu rút tiền.
        withdraw = withdrawRequestRepository.save(withdraw);

        return BaseResponse.success_data(
                "Duyệt rút tiền thành công",
                WithdrawResponse.from(withdraw)
        );
    }

    /**
     * Chức năng: Admin từ chối yêu cầu rút tiền.
     *
     * Khi từ chối:
     * 1. Kiểm tra yêu cầu còn PENDING không.
     * 2. Lấy ví seller và khóa ví.
     * 3. Hoàn lại tiền vào ví seller.
     * 4. Tạo lịch sử giao dịch hoàn tiền.
     * 5. Cập nhật trạng thái yêu cầu thành REJECTED.
     */
    @Override
    @Transactional
    public BaseResponse<WithdrawResponse> rejectWithdraw(
            Long withdrawId,
            AdminWithdrawDecisionRequest request
    ) {

        // Tìm yêu cầu rút tiền theo ID và khóa bản ghi để tránh xử lý trùng.
        WithdrawRequest withdraw = withdrawRequestRepository.findByIdForUpdate(withdrawId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu rút tiền"));

        // Nếu yêu cầu không còn PENDING thì không cho xử lý nữa.
        if (withdraw.getStatus() != WithdrawStatus.PENDING) {
            throw new RuntimeException("Yêu cầu rút tiền đã được xử lý");
        }

        // Lấy ví seller và khóa ví để cập nhật số dư an toàn.
        Wallet wallet = walletRepository.findByUserIdForUpdate(withdraw.getSeller().getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví seller"));

        // Hoàn lại số tiền rút vào ví seller.
        wallet.setBalance(
                (wallet.getBalance() == null ? 0L : wallet.getBalance()) + withdraw.getAmount()
        );

        // Lưu ví sau khi hoàn tiền.
        walletRepository.save(wallet);

        // Tạo lịch sử giao dịch hoàn tiền.
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setType(WalletTransactionType.TOP_UP);
        tx.setAmount(withdraw.getAmount());
        tx.setDescription("Hoàn tiền do admin từ chối yêu cầu rút #" + withdraw.getWithdrawId());

        // Lưu lịch sử giao dịch ví.
        walletTransactionRepository.save(tx);

        // Cập nhật trạng thái yêu cầu rút tiền sang REJECTED.
        withdraw.setStatus(WithdrawStatus.REJECTED);

        // Lưu ghi chú từ admin nếu có.
        withdraw.setAdminNote(request == null ? null : request.getAdminNote());

        // Lưu người xử lý.
        withdraw.setProcessedBy(currentEmail());

        // Lưu thời gian xử lý.
        withdraw.setProcessedAt(LocalDateTime.now());

        // Lưu lại yêu cầu rút tiền.
        withdraw = withdrawRequestRepository.save(withdraw);

        return BaseResponse.success_data(
                "Từ chối rút tiền thành công, tiền đã hoàn lại ví seller",
                WithdrawResponse.from(withdraw)
        );
    }

    /**
     * Chức năng: Lấy ví của seller.
     *
     * Nếu seller chưa có ví thì tự động tạo ví mới:
     * - Số dư ban đầu = 0
     * - Trạng thái ví = ACTIVE
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
     * Chức năng: Lấy email hoặc username của admin đang đăng nhập.
     *
     * Dữ liệu được lấy từ Spring SecurityContext.
     */
    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}