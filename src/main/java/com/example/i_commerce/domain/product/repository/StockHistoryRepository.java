package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.StockHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

    @Query("""
    SELECT sh FROM StockHistory sh
    JOIN FETCH sh.stock s
    JOIN FETCH s.productItem
    WHERE sh.orderId = :orderId
    AND sh.changeType = 'DEDUCT'
    """)
    List<StockHistory> findDeductHistoriesByOrderId(@Param("orderId") Long orderId);


}
