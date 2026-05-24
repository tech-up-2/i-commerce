package com.example.i_commerce.domain.member.entity;


import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import com.example.i_commerce.domain.member.service.store.dto.StoreUpdateRequest;
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
@Table(name = "stores")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "seller_id", nullable = false, updatable = false)
//    private Seller seller;

    @Column(nullable = false, name = "seller_id")
    private Long sellerId;

    @Column(nullable = false, length = 50)
    private String storeName;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StoreStatus storeStatus = StoreStatus.CLOSE;

    public void update(StoreUpdateRequest request) {
        this.storeName = request.storeName();
        this.phoneNumber = request.phoneNumber();
        this.storeStatus = request.storeStatus();
    }

    public void delete() {
        this.storeStatus = StoreStatus.WITHDRAW;
        super.delete();
    }
}
