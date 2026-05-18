package com.example.i_commerce.domain.member.service.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "DeliveryAddressRequest", description = "배송지 수정 요청")
public record UpdateDeliveryAddressRequest(
    @Size(max = 50, message = "배송지 별칭은 50자 이하로 입력해주세요.")
    String label,

    @Size(max = 50, message = "수령인 이름은 50자 이하로 입력해주세요.")
    String recipientName,

    @Size(max = 20, message = "전화번호는 20자 이하로 입력해주세요.")
    String recipientPhone,

    @Size(max = 10, message = "우편번호는 10자 이하로 입력해주세요.")
    String zipCode,

    @Size(max = 255, message = "도로명 주소는 255자 이하로 입력해주세요.")
    String roadAddress,

    @Size(max = 255, message = "지번 주소는 255자 이하로 입력해주세요.")
    String jibunAddress,

    @Size(max = 255, message = "상세 주소는 255자 이하로 입력해주세요.")
    String detailAddress,

    @Size(max = 255, message = "참고 주소는 255자 이하로 입력해주세요.")
    String extraAddress,

    @NotNull(message = "기본 배송지 여부는 필수입니다.")
    Boolean isDefault,

    @Size(max = 255, message = "배송 메모는 255자 이하로 입력해주세요.")
    String deliveryMemo
) {

}
