package com.example.i_commerce.domain.member.entity;

import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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

    @Column(nullable = false)//암호화
    private byte[] bankName;

    @Column(nullable = false)//암호화
    private byte[] bankAccount;

    @Column(nullable = false)//암호화
    private byte[] depositorName;

//    @Builder.Default
//    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Store> stores = new ArrayList<>();

    public void update(
        String businessName,
        String businessNumber,
        String mailOrderRegistrationNumber,
        String ownerName,
        String phoneNumber,
        byte[] bankName,
        byte[] bankAccount,
        byte[] depositorName
    ) {
        this.businessName = businessName;
        this.businessNumber = businessNumber;
        this.mailOrderRegistrationNumber = mailOrderRegistrationNumber;
        this.ownerName = ownerName;
        this.phoneNumber = phoneNumber;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.depositorName = depositorName;
    }

    public void changeStatus(SellerStatus sellerStatus) {
        this.sellerStatus = sellerStatus;
    }

    public void approve() {
        this.sellerStatus = SellerStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void delete() {
        this.sellerStatus = SellerStatus.WITHDRAW;
        super.delete();
    }
}
