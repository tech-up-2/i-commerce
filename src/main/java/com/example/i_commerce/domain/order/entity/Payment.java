package com.example.i_commerce.domain.order.entity;

import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

//    @Enumerated(EnumType.STRING)
//    private  method;

    private Integer amount;

    private Integer cancelableAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus payStatus;

    @Column(name = "pg_tid", length = 100)
    private String pgTid; // PG사 거래 고유 번호

    @Builder.Default
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentHistory> paymentHistories = new ArrayList<>();

    public void changePayStatus(PaymentStatus status) {
        this.payStatus = status;
    }

    public void completePayment(String pgTid) {
        if (this.pgTid != null) {
            throw new IllegalStateException("이미 결제 완료된 건입니다.");
        }
        this.pgTid = pgTid;
        this.cancelableAmount = this.amount;
        this.payStatus = PaymentStatus.PAID; // 상태 변경도 한 번에 처리 가능
    }

    public void cancelPayment(Integer cancelAmount) {
        this.cancelableAmount -= cancelAmount;
        this.payStatus = PaymentStatus.CANCELLED;
    }

}
