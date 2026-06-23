package com.example.i_commerce.domain.member.service.loginHistory;

import com.example.i_commerce.domain.member.service.loginHistory.dto.MemberLoginHistoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLoginHistoryEventHandler {

    private final LoginLogService loginLogService;

    @Async("loginHistoryExecutor")
    @EventListener
    public void handle(MemberLoginHistoryEvent event) {
        try {
            loginLogService.writeMemberLoginHistory(
                event.memberId(),
                event.loginResult(),
                event.ipAddress(),
                event.loginAt(),
                event.failReason()
            );
        } catch (Exception e) {
            log.error("로그인 이력 저장 실패. memberId={}, result={}",
                event.memberId(), event.loginResult(), e);
        }
    }
}