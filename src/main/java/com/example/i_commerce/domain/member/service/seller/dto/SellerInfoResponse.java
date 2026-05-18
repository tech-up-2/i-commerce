package com.example.i_commerce.domain.member.service.seller.dto;

import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import java.time.LocalDateTime;

public record SellerInfoResponse(
    String businessName,
    String businessNumber,
    String mailOrderRegistrationNumber,
    String ownerName,
    String phoneNumber,
    SellerStatus sellerStatus,
    LocalDateTime approvedAt,
    String bankName,
    String bankAccount,
    String depositorName
) {

}
