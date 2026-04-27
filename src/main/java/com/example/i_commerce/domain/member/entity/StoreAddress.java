package com.example.i_commerce.domain.member.entity;

import com.example.i_commerce.domain.member.entity.enums.AddressType;
import com.example.i_commerce.global.common.entity.BaseEntity;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_addresses")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

//    @Column(nullable = false)
//    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType addressType;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false, length = 20)
    private String addressPhoneNumber;

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false, length = 255)
    private String roadAddress;

    @Column(length = 255)
    private String jibunAddress;

    @Column(nullable = false, length = 255)
    private String detailAddress;

    @Column(length = 255)
    private String extraAddress;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDefault = false;
}
