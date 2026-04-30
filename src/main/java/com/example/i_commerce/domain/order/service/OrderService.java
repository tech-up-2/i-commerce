package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.OrderProduct;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.OrderCreatedEvent;
import com.example.i_commerce.domain.order.exception.OrderErrorCode;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.OrderItemDto;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductItemRepository productItemRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher publisher;

    public ApiResponse<Void> createOrder(CreateOrderRequest dto) {

        // 주문 생성
        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        List<Long> ids = dto.items().stream().map(OrderItemDto::productId).toList();
        List<ProductItem> productItems = productItemRepository.findAllById(ids);

        Map<Long, ProductItem> productMap = productItems.stream().collect(Collectors.toMap(ProductItem::getId, Function.identity()));

        List<OrderProduct> orderProducts = dto.items().stream()
                .map(orderItemDto -> {
                    ProductItem productItem = productMap.get(orderItemDto.productId());

                    if(productItem == null) {
                        throw new AppException(ProductErrorCode.PRODUCT_NOT_FOUND);
                    }

                    return OrderProduct.builder()
                            .productSkuId(productItem.getId())
                            .productName(productItem.getProduct().getName())
                            .orderPrice(productItem.getPrice())
                            .count(orderItemDto.quantity())
                            .build();

                })
                .toList();

        int totalPrice = orderProducts.stream()
                .mapToInt(op -> op.getOrderPrice() * op.getCount())
                .sum();

        DeliveryAddress deliveryAddress = member.getDeliveryAddresses().stream()
                .filter(DeliveryAddress::getIsDefault)
                .findFirst()
                .orElseThrow(() -> new AppException(MemberErrorCode.DEFAULT_ADDRESS_NOT_FOUND));

        Order order = orderRepository.save(Order.builder()
                .userId(member.getId())
                .orderStatus(OrderStatus.PENDING)
                .orderProducts(orderProducts)
                .totalProductAmount(totalPrice) // 총 금액
                .totalPayAmount(totalPrice) // 실제 결제 금액
                .zipCode(deliveryAddress.getZipCode())
                .address(deliveryAddress.getRoadAddress())
                .addressDetail(deliveryAddress.getDetailAddress())
                .build());

        Payment payment = paymentRepository.save(Payment.builder()
                .order(order)
                .amount(totalPrice)
                .payStatus(PaymentStatus.READY)
                .build());

       //TODO : 재고 차감
        publisher.publishEvent(new OrderCreatedEvent(order.getId(), payment.getId(), member.getId(), totalPrice));

        return ApiResponse.success();

    }

    public void updateOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(OrderErrorCode.ORDER_TEMP_ERROR));

        order.changeOrderStatus(OrderStatus.CONFIRMED);
    }
}
