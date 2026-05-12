package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.OrderProduct;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.event.dto.PaymentCompletedEvent;
import com.example.i_commerce.domain.order.exception.OrderErrorCode;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final ProductItemRepository productItemRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createDelivery(PaymentCompletedEvent event) {

        Order order = orderRepository.findById(event.payment().getOrder().getId()).orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_FOUND));

        List<Long> productIds = order.getOrderProducts().stream()
                .map(OrderProduct::getProductSkuId).toList();
        // TODO : 판매자 정보 가져오기
        Map<Long, Long> sellerMap = productItemRepository.findAllById(productIds).stream().collect(Collectors.toMap(
                ProductItem::getId, productItem -> productItem.getProduct().getStoreId()));

        Map<Long, List<OrderProduct>> itemsBySeller = order.getOrderProducts().stream()
                .collect(Collectors.groupingBy(item -> sellerMap.get(item.getProductSkuId())));

        itemsBySeller.forEach((storeId, items) -> {
            String groupId = UUID.randomUUID().toString();

            items.forEach(item -> {
                Delivery delivery = deliveryRepository.save(Delivery.builder()
                        .order(item.getOrder())
                        .deliveryGroupId(groupId)
                        .storeId(storeId) // 조회한 판매자 ID 할당
                        .deliveryStatus(DeliveryStatus.PREPARING)
                        .build());

                item.assignDelivery(delivery);
            });
        });

    }
}
