package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.event.dto.DeliveryStatusChangedEvent;
import com.example.i_commerce.domain.order.exception.DeliveryErrorCode;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.service.dto.DeliveryResponse;
import com.example.i_commerce.domain.order.service.dto.DeliveryShipRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerDeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final StoreRepository storeRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public ApiResponse<Void> shipDelivery(Long sellerId, DeliveryShipRequest request) {

        Store store = storeRepository.findById(request.storeId()).orElseThrow();

        if(!Objects.equals(store.getSellerId(), sellerId)) {
            throw new AppException(DeliveryErrorCode.STORE_FORBIDDEN);
        }

        Delivery delivery = deliveryRepository.findBWithOrderById(request.deliveryId())
                .orElseThrow(() -> new AppException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        if (!request.orderId().equals(delivery.getOrder().getId())) {
            throw new AppException(DeliveryErrorCode.CANNOT_SHIP_STATUS);
        }

        delivery.registerTrackingNumber(request.trackingNumber());
        publisher.publishEvent(new DeliveryStatusChangedEvent(delivery.getOrder().getId(), delivery.getId(), delivery.getDeliveryStatus()));
        return ApiResponse.success();
    }

    @Transactional(readOnly = true)
    public ApiResponse<Page<DeliveryResponse>> getDeliveryList(Long sellerId, Long storeId, DeliveryStatus status, Pageable pageable) {

        Store store = storeRepository.findById(storeId).orElseThrow();

        if(!Objects.equals(store.getSellerId(), sellerId)) {
            throw new AppException(DeliveryErrorCode.DELIVERY_NOT_FOUND);
        }

        Page<Delivery> deliveryPage = deliveryRepository.findAllByStoreId(storeId, status, pageable);
        return ApiResponse.success(deliveryPage.map(delivery ->
            DeliveryResponse.of(delivery.getOrder().getId(), delivery)
        ));
    }

    @Transactional
    public void completeDelivery(Long orderId, Long deliveryId) {

        Delivery delivery = deliveryRepository.findBWithOrderById(deliveryId).orElseThrow(() ->
                new AppException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        if(!Objects.equals(delivery.getOrder().getId(), orderId)) {
            throw new AppException(DeliveryErrorCode.INVALID_DELIVERY_ORDER);
        }

        delivery.changeDeliveryStatus(DeliveryStatus.ARRIVED);

        publisher.publishEvent(new DeliveryStatusChangedEvent(orderId, deliveryId, DeliveryStatus.ARRIVED));
    }
}
