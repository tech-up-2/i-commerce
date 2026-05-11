package com.example.i_commerce.domain.order.repository;

import com.example.i_commerce.domain.order.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
}
