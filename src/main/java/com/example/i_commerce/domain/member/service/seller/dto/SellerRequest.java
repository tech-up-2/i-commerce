package com.example.i_commerce.domain.member.service.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "SellerRequest", description = "판매자 신청/수정 요청")
public record SellerRequest(
    @Schema(description = "상호명", example = "kt마켓")
    @NotBlank
    String businessName,

    @Schema(description = "사업자번호", example = "123-54-67890")
    @NotBlank
    String businessNumber,// '-' 없이 10자리 포함하면 12자리

    @Schema(description = "통신 판매업 신고번호", example = "2025-서울용산-01075")
    @NotBlank
    String mailOrderRegistrationNumber,

    @Schema(description = "대표자명", example = "홍길동")
    @NotBlank
    String ownerName,

    @Schema(description = "전화번호", example = "021234567")
    @NotBlank
    String phoneNumber,

    @Schema(description = "정산은행", example = "국민은행")
    @NotBlank
    String bankName,

    @Schema(description = "계좌번호", example = "1234567890")
    @NotBlank
    String bankAccount,

    @Schema(description = "예금주명", example = "홍길동")
    @NotBlank
    String depositorName
) {

}
