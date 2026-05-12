package com.example.i_commerce.domain.order.service.dto;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.OrderProduct;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import lombok.Builder;

@Builder
public record OrderDetailResponse(
        @Schema(description = "주문 상태", example = "COMPLETED")
        OrderStatus orderStatus,

        @Schema(description = "주문 상품 목록")
        List<OrderProductDetail> items,

        @Schema(description = "결제 상세 정보")
        PaymentInfo payment
) {
        public static OrderDetailResponse of(Order order, List<OrderProductDetail> orderProductDetails, PaymentInfo paymentInfo) {
                return OrderDetailResponse.builder()
                        .orderStatus(order.getOrderStatus())
                        .items(orderProductDetails)
                        .payment(paymentInfo)
                        .build();
        }

        @Builder
        public record OrderProductDetail(
                @Schema(description = "상품 ID", example = "101")
                Long productId,

                @Schema(description = "상품명", example = "스프링 부트 완벽 가이드")
                String productName,

                @Schema(description = "주문 수량", example = "1")
                Integer quantity,

                @Schema(description = "상품 단가", example = "139600")
                Integer price,

                @Schema(description = "상태", example = "배송 중")
                String status

        ) {
                public static OrderProductDetail of(OrderProduct orderProduct) {

                        return OrderProductDetail.builder()
                                .productId(orderProduct.getProductSkuId())
                                .productName(orderProduct.getProductName())
                                .quantity(orderProduct.getCount())
                                .price(orderProduct.getOrderPrice())
                                .status(Objects.nonNull(orderProduct.getDelivery()) ? orderProduct.getDelivery().getDeliveryStatus().getStatus() : "결제 대기중")
                                .build();
                }
        }

        @Builder
        public record PaymentInfo(

                @Schema(description = "결제 고유 ID", example = "1")
                Long paymentId,

                @Schema(description = "총 결제 금액", example = "139600")
                Integer totalAmount,

                @Schema(description = "토스 결제 키 (취소 시 필요)", example = "payment_pk_12345")
                String paymentKey,

//                @Schema(description = "결제 수단", example = "CARD")
//                String method,


                @Schema(description = "취소 가능 잔액 (부분 취소 시 사용)", example = "139600")
                Integer cancelableAmount

//                @Schema(description = "매출 전표 URL", example = "https://tosspayments.com...")
//                String receiptUrl
        ) {
                public static PaymentInfo of(Payment payment) {
                        return PaymentInfo.builder()
                                .paymentId(payment.getId())
                                .totalAmount(payment.getAmount())
                                .paymentKey(payment.getPgTid())
                                .cancelableAmount(payment.getCancelableAmount())
                                .build();
                }
        }


}
