package com.example.i_commerce.domain.product.entity;

import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "product_variants")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductAttribute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_item_id", nullable = false)
    private ProductItem productItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private Integer displayOrder;


    public static ProductAttribute of(
        Attribute attribute,
        String displayName,
        Integer displayOrder
    ) {
        return ProductAttribute.builder()
            .attribute(attribute)
            .displayName(displayName)
            .displayOrder(displayOrder)
            .build();
    }

    void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }


}
