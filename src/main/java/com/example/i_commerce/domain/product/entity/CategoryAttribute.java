package com.example.i_commerce.domain.product.entity;


import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "category_attributes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_category_attribute",
        columnNames = {"category_id", "attribute_id"}
    )
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryAttribute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    @Builder.Default
    private Boolean required = false;

    public static CategoryAttribute of(
        Category category,
        Attribute attribute,
        Boolean required
    ) {
        return CategoryAttribute.builder()
            .category(category)
            .attribute(attribute)
            .required(required)
            .build();
    }

}
