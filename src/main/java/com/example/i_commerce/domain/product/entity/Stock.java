package com.example.i_commerce.domain.product.entity;

import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.common.entity.BaseEntity;
import com.example.i_commerce.global.exception.AppException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(length = 50)
    private String status;

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

}
