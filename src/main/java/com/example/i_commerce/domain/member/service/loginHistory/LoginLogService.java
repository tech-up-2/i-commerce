package com.example.i_commerce.domain.member.service.loginHistory;

import com.example.i_commerce.domain.member.entity.AdminLoginHistory;
import com.example.i_commerce.domain.member.entity.FailedLoginAttempt;
import com.example.i_commerce.domain.member.entity.UserLoginHistory;
import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
import com.example.i_commerce.domain.member.entity.enums.LoginResult;
import com.example.i_commerce.domain.member.repository.AdminLoginHistoryRepository;
import com.example.i_commerce.domain.member.repository.EmailBlacklistRepository;
import com.example.i_commerce.domain.member.repository.UserLoginHistoryRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final UserLoginHistoryRepository userLoginHistoryRepository;
    private final AdminLoginHistoryRepository adminLoginHistoryRepository;

    private static final int MAX_FAILURE_COUNT = 5;
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(10);

    private final EmailBlacklistRepository loginBlacklistRepository;

    private final Map<String, FailedLoginAttempt> failedLoginCache = new ConcurrentHashMap<>();

    //히스토리 기록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeMemberLoginHistory(
        Long memberId,
        LoginResult loginResult,
        String ipAddress,
        LocalDateTime time,
        LoginFailReason failReason) {
        UserLoginHistory history = UserLoginHistory.builder()
            .memberId(memberId)
            .loginResult(loginResult)
            .ipAddress(ipAddress)
            .loginAt(time)
            .failReason(failReason)
            .build();

        userLoginHistoryRepository.save(history);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAdminLoginHistory(
        Long memberId,
        LoginResult loginResult,
        String ipAddress,
        LocalDateTime time,
        LoginFailReason failReason) {
        AdminLoginHistory history = AdminLoginHistory.builder()
            .adminId(memberId)
            .loginResult(loginResult)
            .ipAddress(ipAddress)
            .loginAt(time)
            .failReason(failReason)
            .build();

        adminLoginHistoryRepository.save(history);
    }

    //매시 정각마다 Email 블랙리스트에서 만료시간이 된 토큰 청소
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void cleanExpiredEmail() {
        userLoginHistoryRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        adminLoginHistoryRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    /*
    로그인 실패상황(비밀번호 틀림)
1. 캐싱된 email 해시맵 탐색해서 있는지 확인
있으면 2, 없으면 3
2. email해싱이 있으면 count+=1
3. email해칭이 없으면 캐싱에 추가하고 count=1 설정
4. count 가 5이상이 되었을 때 해당 email해싱을 blacklist에 기록하고 캐싱에서 삭제

캐싱해둔 email은 expiresat에 따라 주기적으로  삭제
     */

    //로그인 실패횟수 탐색해서 5회이상이면 blacklist에 기록
    //캐싱 필요할 것 같은데 해시맵사용?
    @Transactional(readOnly = true)
    public boolean
}
