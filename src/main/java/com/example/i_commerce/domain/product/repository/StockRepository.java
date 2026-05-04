package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.entity.Stock;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT s FROM Stock s
    WHERE s.productItem.id IN :productItemIds
    ORDER BY s.productItem.id ASC
    """)
    List<Stock> findAllByProductItemIdsWithLock(@Param("productItemIds") List<Long> productItemIds);


}
