package com.example.i_commerce.domain.review.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportEventListener {

    @EventListener
    public void handleReportApprovedEvent(ReviewStatusChangedEvent event) {
        log.info("[이벤트 수신] 사용자 {}에게 알림 발송 시작: {}", event.getReporterId(), event.getMessage());
    }

}
