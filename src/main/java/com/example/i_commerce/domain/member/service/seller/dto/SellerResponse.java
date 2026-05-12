package com.example.i_commerce.domain.member.service.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "SellerResponse", description = "판매자 신청 응답")
public record SellerResponse(
    @Schema(description = "판매자 ID", example = "1")
    Long sellerId,

    @Schema(description = "신청시각", example = "2026-04-24 13:00:00")
    LocalDateTime createdAt
) {

}
