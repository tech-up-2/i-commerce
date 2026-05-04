package com.example.i_commerce.domain.order.repository;

import com.example.i_commerce.domain.order.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
