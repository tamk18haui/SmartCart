package com.gr6.SmartCart.module_v2.promotion.service.Impl;


import com.gr6.SmartCart.common.domain.Shop;
import com.gr6.SmartCart.common.domain.Voucher;
import com.gr6.SmartCart.common.enums.DiscountType;
import com.gr6.SmartCart.common.enums.VoucherStatus;
import com.gr6.SmartCart.module_v2.promotion.dto.VoucherRequest;
import com.gr6.SmartCart.module_v2.promotion.dto.VoucherResponse;
import com.gr6.SmartCart.module_v2.promotion.service.VoucherV2Service;
import com.gr6.SmartCart.modules.finance_core.repository.VoucherRepository;
import com.gr6.SmartCart.modules.identity.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherV2ServiceImpl implements VoucherV2Service {

    private final VoucherRepository voucherRepository;
    private final ShopRepository shopRepository;

    @Override
    public List<VoucherResponse> getAllVouchersBySeller(String email) {
        Shop shop = getActiveShopByEmail(email);
        return voucherRepository.findByShop_ShopId(shop.getShopId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VoucherResponse createVoucher(String email, VoucherRequest request) {
        Shop shop = getActiveShopByEmail(email);
        validateVoucherLogic(shop.getShopId(), request, null);

        // Khởi tạo không dùng Builder để tránh lỗi Lombok
        Voucher voucher = new Voucher();
        voucher.setShop(shop);
        voucher.setCode(request.getCode().toUpperCase());
        voucher.setDiscountType(request.getDiscountType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : 0L);
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setUsedCount(0);
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setStatus(VoucherStatus.ACTIVE);

        return mapToResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(String email, Long voucherId, VoucherRequest request) {
        Shop shop = getActiveShopByEmail(email);
        Voucher existingVoucher = getVoucherByShopAndId(shop.getShopId(), voucherId);

        if (existingVoucher.getUsedCount() > 0) {
            if (!existingVoucher.getCode().equalsIgnoreCase(request.getCode()) ||
                    !existingVoucher.getDiscountType().equals(request.getDiscountType())) {
                throw new RuntimeException("Voucher đã có lượt sử dụng. Bạn chỉ được phép cập nhật số lượng hoặc thời gian.");
            }
        }

        validateVoucherLogic(shop.getShopId(), request, voucherId);

        existingVoucher.setCode(request.getCode().toUpperCase());
        existingVoucher.setDiscountType(request.getDiscountType());
        existingVoucher.setDiscountValue(request.getDiscountValue());
        existingVoucher.setMinOrderValue(request.getMinOrderValue());
        existingVoucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        existingVoucher.setUsageLimit(request.getUsageLimit());
        existingVoucher.setStartDate(request.getStartDate());
        existingVoucher.setEndDate(request.getEndDate());

        return mapToResponse(voucherRepository.save(existingVoucher));
    }

    @Override
    @Transactional
    public void deactivateVoucher(String email, Long voucherId) {
        Shop shop = getActiveShopByEmail(email);
        Voucher existingVoucher = getVoucherByShopAndId(shop.getShopId(), voucherId);

        existingVoucher.setStatus(VoucherStatus.HIDDEN);
        voucherRepository.save(existingVoucher);
    }

    // --- CÁC HÀM TIỆN ÍCH DÙNG CHUNG ---

    private Shop getActiveShopByEmail(String email) {
        Shop shop = shopRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Tài khoản này chưa có gian hàng."));
        if (!shop.getStatus().name().equals("ACTIVE")) {
            throw new RuntimeException("Gian hàng của bạn đang không hoạt động.");
        }
        return shop;
    }

    private Voucher getVoucherByShopAndId(Long shopId, Long voucherId) {
        return voucherRepository.findByVoucherIdAndShop_ShopId(voucherId, shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Voucher này trong gian hàng của bạn."));
    }

    private void validateVoucherLogic(Long shopId, VoucherRequest request, Long excludeVoucherId) {
        if (request.getStartDate().isAfter(request.getEndDate()) || request.getStartDate().isEqual(request.getEndDate())) {
            throw new RuntimeException("Ngày bắt đầu phải diễn ra trước ngày kết thúc.");
        }

        boolean isDuplicate = voucherRepository.findByShop_ShopId(shopId).stream()
                .anyMatch(v -> v.getCode().equalsIgnoreCase(request.getCode())
                        && (excludeVoucherId == null || !v.getVoucherId().equals(excludeVoucherId)));

        if (isDuplicate) {
            throw new RuntimeException("Mã voucher đã tồn tại trong gian hàng của bạn.");
        }

        if (request.getDiscountType() == DiscountType.PERCENT) {
            if (request.getDiscountValue() > 100) {
                throw new RuntimeException("Giảm theo phần trăm không được vượt quá 100%.");
            }
            if (request.getMaxDiscountAmount() == null || request.getMaxDiscountAmount() <= 0) {
                throw new RuntimeException("Vui lòng thiết lập 'Số tiền giảm tối đa' cho loại giảm phần trăm.");
            }
        }
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .voucherId(voucher.getVoucherId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .status(voucher.getStatus().name())
                .build();
    }
}