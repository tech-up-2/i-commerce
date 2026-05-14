package com.example.i_commerce.domain.member.entity;

import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.service.seller.dto.SellerRequest;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sellers")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller extends BaseEntity {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId  //Seller의 PK를 Member의 PK와 동기화
    @JoinColumn(name = "id")
    private Member member;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String businessNumber;

    @Column(nullable = false)
    private String mailOrderRegistrationNumber;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String phoneNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerStatus sellerStatus = SellerStatus.PENDING;

    private LocalDateTime approvedAt;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String bankAccount;

    @Column(nullable = false)
    private String depositorName;

    @Builder.Default
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Store> stores = new ArrayList<>();

    public void update(SellerRequest request) {
        this.businessName = request.businessName();
        this.businessNumber = request.businessNumber();
        this.mailOrderRegistrationNumber = request.mailOrderRegistrationNumber();
        this.ownerName = request.ownerName();
        this.phoneNumber = request.phoneNumber();
        this.bankName = request.bankName();
        this.bankAccount = request.bankAccount();
        this.depositorName = request.depositorName();
    }

    public void changeSellerStatus(SellerStatus sellerStatus) {
        this.sellerStatus = sellerStatus;
    }
}
