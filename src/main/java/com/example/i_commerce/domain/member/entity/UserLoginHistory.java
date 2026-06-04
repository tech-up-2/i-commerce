package com.example.i_commerce.domain.member.entity;


import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
import com.example.i_commerce.domain.member.entity.enums.LoginResult;
import com.example.i_commerce.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_login_histories")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, name = "user_id")
    private Long memberId;

    //로그인 성공여부
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginResult loginResult;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = true)
    private LocalDateTime loginAt;

    //로그인 실패 사유
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private LoginFailReason failReason;
}
