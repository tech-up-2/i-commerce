package com.example.i_commerce.domain.order.repository;

import com.example.i_commerce.domain.order.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
    List<Order> findAllByUserId(Long userId);
}

