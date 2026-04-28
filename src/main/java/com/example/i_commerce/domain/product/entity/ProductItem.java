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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_items")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private Integer price;

    private String mainImageUrl;

    @Column(length = 50, nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "option_value_1_id")
    private ProductOptionValue optionValue1;

    @ManyToOne
    @JoinColumn(name = "option_value_2_id")
    private ProductOptionValue optionValue2;

    @Builder.Default
    @OneToMany(mappedBy = "productItem", cascade = CascadeType.ALL)
    private List<ProductAttribute> attributes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "productItem", cascade = CascadeType.ALL)
    private List<Stock> stocks = new ArrayList<>();



}
