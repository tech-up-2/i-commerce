package com.example.i_commerce.domain.member.entity;

import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_login_histories")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminLoginHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

//    @Column(nullable = false)
//    private Long adminId;

    @Column(nullable = false)
    private Boolean loginResult = false;

    @Column(length = 50)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime loginAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginFailReason failReason; // 회원 실패 사유와 동일한 Enum 사용 가능

    private LocalDateTime logoutAt;
}
