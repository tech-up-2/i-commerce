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
import jakarta.persistence.ManyToOne;
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

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ProductItemStatus status;

    private String displayOptionName;

    @OneToOne(mappedBy = "productItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stock stock;

    @ManyToOne
    @JoinColumn(name = "option_value_1_id")
    private ProductOptionValue optionValue1;

    @ManyToOne
    @JoinColumn(name = "option_value_2_id")
    private ProductOptionValue optionValue2;

    @Column(nullable = false)
    private boolean isDefault;

    @Builder.Default
    @OneToMany(mappedBy = "productItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttribute> attributes = new ArrayList<>();


    public static ProductItem of(
        String sku,
        Integer price,
        String displayOptionName,
        ProductOptionValue optionValue1,
        ProductOptionValue optionValue2,
        Boolean isDefault
    ) {
        return ProductItem.builder()
            .sku(sku)
            .price(price)
            .displayOptionName(displayOptionName)
            .optionValue1(optionValue1)
            .optionValue2(optionValue2)
            .isDefault(isDefault)
            .build();
    }

    public void addAttribute(ProductAttribute attribute) {
        this.attributes.add(attribute);
        attribute.setProductItem(this);
    }

    public void initStock(Integer quantity) {
        if(this.stock != null) {
            throw new AppException(ProductErrorCode.STOCK_ALREADY_INITIALIZED);
        }
        this.stock = Stock.of(this, quantity);
    }

    public void changeStatus(ProductItemStatus status) {
        this.status = status;
    }

    void setProduct(Product product) {
        this.product = product;
    }


}
