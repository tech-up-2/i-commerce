package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.exception.DeliveryErrorCode;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.service.dto.DeliveryShipRequest;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerDeliveryService {
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public void shipDelivery(DeliveryShipRequest request) {
        Delivery delivery = deliveryRepository.findById(request.deliveryId())
                .orElseThrow(() -> new AppException(DeliveryErrorCode.DELIVERY_NOT_FOUND));

        if (delivery.getDeliveryStatus() != DeliveryStatus.PREPARING) {
            throw new AppException(DeliveryErrorCode.CANNOT_SHIP_STATUS);
        }

        delivery.registerTrackingNumber(request.trackingNumber());
    }
}
