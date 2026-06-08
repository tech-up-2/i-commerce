package com.example.i_commerce.domain.member.entity;

import com.example.i_commerce.domain.member.entity.enums.LoginFailState;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class FailedLoginAttempt {

    private static final int BLOCK_THRESHOLD = 5;

    // 실패횟수 카운팅
    private final int count;

    // COUNTING 상태에서는 실패 카운트 만료 시간,
    // BLOCKED 상태에서는 차단 만료 시간으로 사용한다.
    private final LocalDateTime expiresAt;

    //현재 상태
    private final LoginFailState state;

    private FailedLoginAttempt(int count, LocalDateTime expiresAt, LoginFailState state) {
        this.count = count;
        this.expiresAt = expiresAt;
        this.state = state;
    }

    public static FailedLoginAttempt startCounting(LocalDateTime expiresAt) {
        return new FailedLoginAttempt(1, expiresAt, LoginFailState.COUNTING);
    }

    public FailedLoginAttempt blockUntil(LocalDateTime expiresAt) {
        return new FailedLoginAttempt(this.count, expiresAt, LoginFailState.BLOCKED);
    }

    public FailedLoginAttempt increase() {
        return new FailedLoginAttempt(this.count + 1, this.expiresAt, LoginFailState.COUNTING);
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isBlocked(LocalDateTime now) {
        return state == LoginFailState.BLOCKED && expiresAt.isAfter(now);
    }

    public boolean shouldBlock() {
        return count >= BLOCK_THRESHOLD;
    }
}
