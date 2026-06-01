package com.example.i_commerce.domain.member.entity;

import com.example.i_commerce.domain.member.entity.enums.LoginFailState;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FailedLoginAttempt {

    private int count;
    private LocalDateTime expiresAt;
    private LoginFailState state;

    public void increase() {
        this.count++;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }

    public boolean isBlocked(LocalDateTime now) {
        return state == LoginFailState.BLOCKED && expiresAt.isAfter(now);
    }

    public void blockUntil(LocalDateTime expiresAt) {
        this.state = LoginFailState.BLOCKED;
        this.expiresAt = expiresAt;
    }
}
