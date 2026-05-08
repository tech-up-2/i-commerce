package com.example.i_commerce.domain.member.entity;

import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_addresses")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    /**
     * 배송지는 회원 기준으로 조회하므로 user_id는 평문 FK 유지
     */
    @Column(name = "user_id", nullable = false)
    private Long memberId;

    /**
     * 배송지 별칭 ex) 집, 회사, 본가 민감도 낮아서 평문
     */
    @Column(nullable = false)
    private String label;

    /**
     * AES-256-GCM 암호화 저장
     */
    @Column(name = "recipient_name", nullable = false)
    private byte[] recipientName;

    @Column(name = "recipient_phone", nullable = false)
    private byte[] recipientPhone;

    /**
     * 우편번호 필요 시 평문 유지 가능하지만 통일성 위해 암호화 추천
     */
    @Column(name = "zipcode", nullable = false)
    private byte[] zipCode;

    /**
     * 도로명 주소
     */
    @Lob//크기가 255초과 위험이 있을 때 사용
    @Column(name = "road_address", nullable = false)
    private byte[] roadAddress;

    /**
     * 지번 주소
     */
    @Lob
    @Column(name = "jibun_address")
    private byte[] jibunAddress;

    /**
     * 상세 주소
     */
    @Lob
    @Column(name = "detail_address", nullable = false)
    private byte[] detailAddress;

    /**
     * 참고 항목 ex) 공동현관 비밀번호 등
     */
    @Lob
    @Column(name = "extra_address")
    private byte[] extraAddress;

    /**
     * 기본 배송지 여부
     */
    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * 배송 메모
     */
    @Lob
    @Column(name = "delivery_memo")
    private byte[] deliveryMemo;

    public void update(//수정 메서드
        String label,
        byte[] recipientName,
        byte[] recipientPhone,
        byte[] zipCode,
        byte[] roadAddress,
        byte[] jibunAddress,
        byte[] detailAddress,
        byte[] extraAddress,
        byte[] deliveryMemo
    ) {
        this.label = label;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.zipCode = zipCode;
        this.roadAddress = roadAddress;
        this.jibunAddress = jibunAddress;
        this.detailAddress = detailAddress;
        this.extraAddress = extraAddress;
        this.deliveryMemo = deliveryMemo;
    }

    public void changeDefault(boolean bool) {//기본 배송지 변경
        this.isDefault = bool;
    }
}
