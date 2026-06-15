package com.example.i_commerce.domain.order.repository;

import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findAllByOrderId(Long aLong);

    @EntityGraph(attributePaths = {"order"})
    @Query("select d from Delivery d where d.storeId = :storeId and (:status is null or d.deliveryStatus = :status )")
    Page<Delivery> findAllByStoreId(
            @Param("storeId") Long storeId,
            @Param("status") DeliveryStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"order"})
    Optional<Delivery> findWithOrderById(Long id);
}
