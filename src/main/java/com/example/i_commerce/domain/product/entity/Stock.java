package com.example.i_commerce.domain.product.entity;

import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.common.entity.BaseEntity;
import com.example.i_commerce.global.exception.AppException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stocks")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_item_id", nullable = false, unique = true)
    private ProductItem productItem;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StockStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
    private List<StockHistory> histories = new ArrayList<>();

    public static Stock of(ProductItem item, Integer quantity) {
        if(quantity < 0) {
            throw new AppException(ProductErrorCode.NEGATIVE_QUANTITY_NOT_ALLOWED);
        }
        return Stock.builder()
            .productItem(item)
            .quantity(quantity)
            .build();
    }

    public void deduct(int amount, Long orderId) {
        if(this.quantity < amount) {
            throw new AppException(ProductErrorCode.INSUFFICIENT_STOCK);
        }
        this.quantity -= amount;

        if(this.quantity == 0) {
            this.status = StockStatus.OUT_OF_STOCK;
        }

        this.histories.add(StockHistory.ofDeduct(this, amount, orderId));
    }

    public void restore(int amount, Long orderId) {
        this.quantity += amount;
        this.status = StockStatus.IN_STOCK;
        this.histories.add(StockHistory.ofRestore(this, amount, orderId));
    }

    public boolean isOutOfStock() {
        return this.status == StockStatus.OUT_OF_STOCK;
    }

}






