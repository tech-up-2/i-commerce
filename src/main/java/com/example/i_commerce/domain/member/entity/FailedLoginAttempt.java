package com.example.i_commerce.domain.member.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FailedLoginAttempt {

    private int count;
    private LocalDateTime expiresAt;

    public void increase() {
        this.count++;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt.isBefore(now);
    }
}
