package com.example.i_commerce.domain.order.repository;

import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByPayStatus(PaymentStatus paymentStatus);

    @Query("select p from Payment p join fetch p.order where p.tossOrderId = :tossOrderId")
    Optional<Payment> findByTossOrderIdWithOrder(@Param("tossOrderId") String tossOrderId);

    @Query("select p from Payment p " +
            "join fetch p.order o " +
            "join fetch o.deliveries " +
            "where p.tossOrderId = :tossOrderId")
    Optional<Payment> findByTossOrderIdWithOrderAndDeliveries(@Param("tossOrderId") String tossOrderId);
}
