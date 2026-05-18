package com.example.i_commerce.domain.member.entity;

import com.example.i_commerce.domain.member.entity.enums.AddressType;
import com.example.i_commerce.domain.member.service.store.dto.StoreAddressRequest;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id")
//    private Store store;

    @Column(nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType addressType;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String addressPhoneNumber;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String roadAddress;

    @Column(nullable = true)
    private String jibunAddress;

    @Column(nullable = false, length = 255)
    private String detailAddress;

    @Column(nullable = true, length = 255)
    private String extraAddress;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDefault = false;

    public void changeDefault(boolean bool) {//기본 배송지 변경
        this.isDefault = bool;
    }

    public void update(StoreAddressRequest dto) {
        this.addressType = dto.addressType();
        this.label = dto.label();
        this.addressPhoneNumber = dto.addressPhoneNumber();
        this.zipCode = dto.zipCode();
        this.roadAddress = dto.roadAddress();
        this.jibunAddress = dto.jibunAddress();
        this.detailAddress = dto.detailAddress();
        this.extraAddress = dto.extraAddress();
    }

    public void delete() {
        isDefault = false;
        super.delete();
    }
}
