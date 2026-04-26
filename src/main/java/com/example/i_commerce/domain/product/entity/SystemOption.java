package com.example.i_commerce.domain.product.entity;

import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "system_options")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String type;

    @Column(length = 255)
    private String value;

    @Column(length = 255)
    private String displayName;

    @Column(length = 50)
    private String inputType;

    @Builder.Default
    @OneToMany(mappedBy = "systemOption")
    private List<ProductOption> productOptions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "systemOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryStandardOption> categoryStandardOptions = new ArrayList<>();
}
