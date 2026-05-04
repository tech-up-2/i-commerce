package com.example.i_commerce.domain.product.entity;

import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_histories")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StockChangeType changeType;

    @Column(nullable = false)
    private Integer changeQuantity;

    @Column(nullable = false)
    private Long orderId;

    public static StockHistory ofDeduct(
        Stock stock, int changeQuantity, Long orderId
    ) {
        return StockHistory.builder()
            .stock(stock)
            .changeType(StockChangeType.DEDUCT)
            .changeQuantity(changeQuantity)
            .orderId(orderId)
            .build();
    }

    public static StockHistory ofRestore(
        Stock stock, int changeQuantity, Long orderId
    ) {
        return StockHistory.builder()
            .stock(stock)
            .changeType(StockChangeType.RESTORE)
            .changeQuantity(changeQuantity)
            .orderId(orderId)
            .build();


    }
}
