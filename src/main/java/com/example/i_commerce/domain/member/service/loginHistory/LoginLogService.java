package com.example.i_commerce.domain.member.service.loginHistory;

import com.example.i_commerce.domain.member.entity.AdminLoginHistory;
import com.example.i_commerce.domain.member.entity.FailedLoginAttempt;
import com.example.i_commerce.domain.member.entity.UserLoginHistory;
import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
import com.example.i_commerce.domain.member.entity.enums.LoginResult;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.AdminLoginHistoryRepository;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.repository.UserLoginHistoryRepository;
import com.example.i_commerce.global.exception.AppException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final AdminRepository adminRepository;

    // 일반 사용자용 실패 캐시
    private final Map<String, FailedLoginAttempt> userFailedLoginCache = new ConcurrentHashMap<>();

    // 관리자용 실패 캐시 (정책을 다르게 가져갈 수 있음)
    private final Map<String, FailedLoginAttempt> adminFailedLoginCache = new ConcurrentHashMap<>();

    //히스토리 기록
    @Transactional
    public void writeMemberLoginHistory(
        Long memberId,
        LoginResult loginResult,
        String ipAddress,
        LocalDateTime time,
        LoginFailReason failReason
    ) {
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

    /*
    로그인 실패상황(비밀번호 틀림)
1. 캐싱된 email 해시맵 탐색해서 있는지 확인
있으면 2-1, 없으면 2-2
2-1. email해싱이 있으면 count+=1
2-2. email해칭이 없으면 캐싱에 추가하고 count=1 설정
3. count 가 5이상이 되었을 때 해당 email해싱의 상태를 block으로 전환

캐싱해둔 email은 expiresat에 따라 주기적으로  삭제
     */
    //짧은 시간안에 여러번의 실패시도가 쌓이면 ex.10회 그러면 차단이 2번 되야하나? 원래는 차단이 되서 여기까지 올 수 없어야 하는데

    //로그인 실패횟수 탐색해서 5회이상이면 blacklist에 기록
    //캐싱 필요할 것 같은데 해시맵사용?
    //member용
    public void userLoginFailedSequence(String emailHashKey) {
        LocalDateTime now = LocalDateTime.now();
        // compute는 변경된 최종 객체를 반환합니다.
        userFailedLoginCache.compute(emailHashKey, (key, failedLoginAttempt) -> {

            // 1. 캐시에 없는 첫 실패인 경우, 차단시간이 만료된 경우
            if (failedLoginAttempt == null || failedLoginAttempt.isExpired(now)) {
                return FailedLoginAttempt.startCounting(now.plusHours(1));
            }

            // 2. 이미 차단이 실행되고 있는 경우
            if (failedLoginAttempt.isBlocked(now)) {
                return failedLoginAttempt;
            }

            // 3. 이미 캐시에 존재하는 경우 (안전하게 값 증가)
            failedLoginAttempt = failedLoginAttempt.increase();

            // 4. 5회 이상 카운팅 시 차단
            if (failedLoginAttempt.shouldBlock()) {
                return failedLoginAttempt.blockUntil(now.plusMinutes(5));
            }

            return failedLoginAttempt;
        });
    }

    //관리자용
    //관리자는 5회이상 실패시 계정 잠금
    public void adminLoginFailedSequence(String emailHashKey) {
        LocalDateTime now = LocalDateTime.now();
        AtomicBoolean shouldLockAdmin = new AtomicBoolean(false);

        // compute는 변경된 최종 객체를 반환합니다.
        adminFailedLoginCache.compute(emailHashKey, (key, failedLoginAttempt) -> {

            // 1. 캐시에 없는 첫 실패인 경우, 차단시간이 만료된 경우
            if (failedLoginAttempt == null || failedLoginAttempt.isExpired(now)) {
                return FailedLoginAttempt.startCounting(now.plusHours(1));
            }

            if (failedLoginAttempt.isBlocked(now)) {
                return failedLoginAttempt;
            }

            // 2. 이미 캐시에 존재하는 경우 (안전하게 값 증가)
            failedLoginAttempt = failedLoginAttempt.increase();

            // 3. 5회 이상 카운팅 시 계정 잠금
            if (failedLoginAttempt.shouldBlock()) {
                shouldLockAdmin.set(true);
                return failedLoginAttempt.blockUntil(now.plusMinutes(5));
            }

            return failedLoginAttempt;
        });

        if (shouldLockAdmin.get()) {
            adminRepository.findByEmailHash(emailHashKey).orElseThrow(() -> new AppException(
                MemberErrorCode.ADMIN_NOT_FOUND)).lock();
        }
    }

    // 10분마다 만료된 캐시를 찾아 삭제하는 스케줄러
    @Scheduled(cron = "0 0/10 * * * *")
    public void cleanupExpiredLoginAttempts() {
        LocalDateTime now = LocalDateTime.now();

        // 부모 map 구조를 안전하게 유지하며 조건에 맞는 엔트리 삭제
        userFailedLoginCache.entrySet().
            removeIf(entry -> entry.getValue().isExpired(now));

        adminFailedLoginCache.entrySet().
            removeIf(entry -> entry.getValue().isExpired(now));
    }

    //로그인 과정에서 emailblacklist탐색
    public void validateNotBlocked(String emailHashKey) {
        LocalDateTime now = LocalDateTime.now();

        FailedLoginAttempt attempt = userFailedLoginCache.get(emailHashKey);

        if (attempt != null && attempt.isBlocked(now)) {
            throw new AppException(MemberErrorCode.LOGIN_TEMPORARILY_BLOCKED);
        }
    }

    public void adminValidateNotBlocked(String emailHashKey) {
        LocalDateTime now = LocalDateTime.now();

        FailedLoginAttempt attempt = adminFailedLoginCache.get(emailHashKey);

        if (attempt != null && attempt.isBlocked(now)) {
            throw new AppException(MemberErrorCode.LOGIN_TEMPORARILY_BLOCKED);
        }
    }
}
