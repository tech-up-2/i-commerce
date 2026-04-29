package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.OrderProduct;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.OrderItemDto;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductItemRepository productItemRepository;
    private final MemberRepository memberRepository;

    public ApiResponse<Void> createOrder(CreateOrderRequest dto) {

        // 주문 생성
        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Long> ids = dto.items().stream().map(OrderItemDto::productId).toList();
        List<ProductItem> productItems = productItemRepository.findAllById(ids);

        Map<Long, ProductItem> productMap = productItems.stream().collect(Collectors.toMap(ProductItem::getId, Function.identity()));

        List<OrderProduct> orderProducts = dto.items().stream()
                .map(orderItemDto -> {
                    ProductItem productItem = productMap.get(orderItemDto.productId());

                    if(productItem == null) {
                        throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
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
                .orElseThrow(() -> new AppException(ErrorCode.DEFAULT_ADDRESS_NOT_FOUND));

        orderRepository.save(Order.builder()
                .userId(dto.memberId())
                .orderStatus(OrderStatus.PENDING)
                .orderProducts(orderProducts)
                .totalProductAmount(totalPrice) // 총 금액
                .totalPayAmount(totalPrice) // 실제 결제 금액
                .zipCode(deliveryAddress.getZipCode())
                .address(deliveryAddress.getRoadAddress())
                .addressDetail(deliveryAddress.getDetailAddress())
                .build());

        // 결제

        // 배송

        return ApiResponse.success();

    }

}
