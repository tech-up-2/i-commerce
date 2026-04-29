package com.example.i_commerce.domain.product.entity;

import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_option_values")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 100, nullable = false)
    private Integer optionOrder;

    @Column(length = 100, nullable = false)
    private String optionName;

    @Column(length = 100, nullable = false)
    private String value;

    @Column(nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @OneToMany(mappedBy = "optionValue1", cascade = CascadeType.ALL)
    private List<ProductItem> items1 = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "optionValue2", cascade = CascadeType.ALL)
    private List<ProductItem> items2 = new ArrayList<>();


    public static ProductOptionValue of(
        Integer optionOrder,
        String optionName,
        String value,
        Integer displayOrder
    ) {
        return ProductOptionValue.builder()
            .optionOrder(optionOrder)
            .optionName(optionName)
            .value(value)
            .displayOrder(displayOrder)
            .build();
    }

    void setProduct(Product product) {
        this.product = product;
    }

}







