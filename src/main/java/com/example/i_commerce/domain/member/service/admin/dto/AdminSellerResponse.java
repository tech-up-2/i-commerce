package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import java.time.LocalDateTime;

public record AdminSellerResponse(
    Long sellerId,
    String email,
    String businessName,
    String businessNumber,
    String ownerName,
    MemberStatus memberStatus,
    SellerStatus sellerStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}