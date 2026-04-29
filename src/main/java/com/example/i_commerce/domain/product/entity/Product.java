package com.example.i_commerce.domain.product.entity;


import com.example.i_commerce.domain.product.enums.ProductStatus;
import com.example.i_commerce.global.common.entity.BaseEntity;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
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
@Table(name = "products")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer optionType;

    @Column(length = 50, nullable = false)
    private String status;

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductItem> items = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductOptionValue> options = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images = new ArrayList<>();

    public static Product of(
        Long storeId,
        Category category,
        String name,
        String description,
        Integer optionType
    ) {
        return Product.builder()
            .storeId(storeId)
            .category(category)
            .name(name)
            .description(description)
            .optionType(optionType)
            .status(ProductStatus.ON_SALE.name())
            .build();
    }

    public void addItem(ProductItem item) {
        if(this.items.stream().anyMatch(
            existingItem -> existingItem.getSku().equals(item.getSku()))) {
            throw new AppException(ErrorCode.DUPLICATED_SKU);
        }
        this.items.add(item);
        item.setProduct(this);
    }

    public void addOptionValue(ProductOptionValue value) {
        this.options.add(value);
        value.setProduct(this);
    }

}
