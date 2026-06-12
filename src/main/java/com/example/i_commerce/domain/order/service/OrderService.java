package com.example.i_commerce.domain.order.service;


import com.example.i_commerce.domain.member.service.delivery.DeliveryAddressService;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressSnapshot;
import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.member.dto.MemberOrderInfo;
import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.OrderProduct;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.exception.OrderErrorCode;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.repository.OrderProductRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest.OrderItemDto;
import com.example.i_commerce.domain.order.service.dto.CreateOrderResponse;
import com.example.i_commerce.domain.order.service.dto.OrderDetailResponse;
import com.example.i_commerce.domain.order.service.dto.OrderDetailResponse.OrderProductDetail;
import com.example.i_commerce.domain.order.service.dto.OrderDetailResponse.PaymentInfo;
import com.example.i_commerce.domain.order.service.dto.OrderProductResponse;
import com.example.i_commerce.domain.order.service.dto.OrderSummaryResponse;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberService memberService;
    private final OrderRepository orderRepository;
    private final ProductItemRepository productItemRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryAddressService deliveryAddressService;
    private final OrderProductRepository orderProductRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public ApiResponse<CreateOrderResponse> createOrder(Long memberId, CreateOrderRequest dto) {

        MemberOrderInfo memberInfo = memberService.getMemberOrderInfo(memberId);
        DeliveryAddressSnapshot addressInfo = deliveryAddressService.getAddressSnapshot(dto.addressId(), memberId);

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

        String cleanedNumber = memberInfo.phoneNumber().replaceAll("[^0-9]", "");
        Order order = Order.builder()
                .userId(memberInfo.id())
                .orderStatus(OrderStatus.PENDING)
                .orderProducts(orderProducts)
                .totalProductAmount(totalPrice) // 총 금액
                .totalPayAmount(totalPrice) // 실제 결제 금액
                .receiverName(memberInfo.name())
                .receiverPhone(cleanedNumber)
                .zipCode(addressInfo.zipCode())
                .address(addressInfo.roadAddress())
                .addressDetail(addressInfo.detailAddress())
                .build();

        orderProducts.forEach(orderProduct -> orderProduct.assignOrder(order));

        orderRepository.save(order);

        Payment payment = paymentRepository.save(Payment.builder()
                .order(order)
                .amount(totalPrice)
                .cancelableAmount(0)
                .payStatus(PaymentStatus.READY)
                .build());

        order.getPayments().add(payment);

        String firstProductName = order.getOrderProducts().stream().findFirst().map(OrderProduct::getProductName).orElse("");

        return ApiResponse.success(CreateOrderResponse.of(order, payment.getTossOrderId(), firstProductName));

    }

    public List<OrderSummaryResponse> getOrderList(Long userId) {
        return orderRepository.findAllByUserId(userId).stream()
                .map(OrderSummaryResponse::of).toList();
    }

    @Transactional
    public OrderDetailResponse getOrderDetail(Long orderId, Long userId) {

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

        List<OrderProductDetail> orderProducts = orderProductRepository.findAllByOrderId(order.getId()).stream()
                .map(OrderProductDetail::of).toList();


        PaymentInfo paymentInfo = order.getPayments().stream()
                .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
                .findFirst()
                .map(PaymentInfo::of)
                .orElse(null);

        return OrderDetailResponse.of(order, orderProducts, paymentInfo);
    }

    @Transactional
    public void validateOrderOwner(String tossOrderId, Long userId) {

        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId)
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        Order order = payment.getOrder();

        if (!order.getUserId().equals(userId)) {
            throw new AppException(OrderErrorCode.ORDER_NOT_OWNED);
        }
    }

    @Transactional(readOnly = true)
    public OrderProductResponse getOrderProductForReview(Long orderProductId) {
        OrderProduct orderProduct = orderProductRepository.findByIdWithOrder(orderProductId)
            .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND));

        return new OrderProductResponse(
            orderProduct.getProductSkuId(),
            orderProduct.getOrder().getUserId(),
            orderProduct.getOrder().getOrderStatus()
        );
    }

    @Transactional
    public void updateOrderStatusByDeliveries(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));
        List<Delivery> deliveries = deliveryRepository.findAllByOrderId(orderId);

        OrderStatus nextStatus = determineOrderStatus(deliveries);

        order.changeOrderStatus(nextStatus);
    }

    private OrderStatus determineOrderStatus(List<Delivery> deliveries) {
        long totalCount = deliveries.size();

        long shippingCount = deliveries.stream()
                .filter(d -> d.getDeliveryStatus() == DeliveryStatus.SHIPPING)
                .count();

        long arrivedCount = deliveries.stream()
                .filter(d -> d.getDeliveryStatus() == DeliveryStatus.ARRIVED)
                .count();

        if (arrivedCount == totalCount) {
            return OrderStatus.DELIVERED;
        }
        if (shippingCount == totalCount) {
            return OrderStatus.SHIPPING;
        }
        if (arrivedCount > 0) {
            return OrderStatus.PARTIAL_DELIVERED;
        }
        if (shippingCount > 0) {
            return OrderStatus.PARTIAL_SHIPPING;
        }

        return OrderStatus.CONFIRMED;
    }
}
