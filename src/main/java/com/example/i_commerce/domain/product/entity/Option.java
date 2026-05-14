package com.example.i_commerce.domain.product.entity;

import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "options",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"type", "value"}
    )
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Option extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String type;

    @Column(length = 100, nullable = false)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private OptionInputType inputType;

    @Builder.Default
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryOption> categoryOptions = new ArrayList<>();

    public static Option of(String type, String value, OptionInputType inputType) {
        return Option.builder()
            .type(type)
            .value(value)
            .inputType(inputType)
            .build();
    }

}
