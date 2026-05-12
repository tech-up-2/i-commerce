package com.example.i_commerce.domain.order.repository;

import com.example.i_commerce.domain.order.entity.Delivery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findAllByOrderId(Long aLong);
}
