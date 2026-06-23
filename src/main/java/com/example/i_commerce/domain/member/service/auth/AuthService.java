package com.example.i_commerce.domain.member.service.auth;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
import com.example.i_commerce.domain.member.entity.enums.LoginResult;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.service.auth.dto.AccountFindEmailRequest;
import com.example.i_commerce.domain.member.service.auth.dto.AccountFindEmailResponse;
import com.example.i_commerce.domain.member.service.auth.dto.LoginRequest;
import com.example.i_commerce.domain.member.service.auth.dto.LoginResponse;
import com.example.i_commerce.domain.member.service.auth.dto.MemberSignUpRequest;
import com.example.i_commerce.domain.member.service.auth.dto.PasswordFindRequest;
import com.example.i_commerce.domain.member.service.auth.dto.PasswordResetRequest;
import com.example.i_commerce.domain.member.service.auth.dto.SignUpResponse;
import com.example.i_commerce.domain.member.service.auth.dto.TokenLogoutRequest;
import com.example.i_commerce.domain.member.service.auth.dto.TokenReissueRequest;
import com.example.i_commerce.domain.member.service.auth.dto.TokenReissueResponse;
import com.example.i_commerce.domain.member.service.auth.dto.UserInfoResponse;
import com.example.i_commerce.domain.member.service.auth.dto.UserUpdateRequest;
import com.example.i_commerce.domain.member.service.auth.dto.WithDrawRequest;
import com.example.i_commerce.domain.member.service.loginHistory.LoginLogService;
import com.example.i_commerce.domain.member.service.loginHistory.dto.MemberLoginHistoryEvent;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.domain.member.tools.PerfTimer;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.BlacklistedTokenService;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.RefreshTokenValidator;
import com.example.i_commerce.global.security.jwt.TokenHashEncoder;
import com.example.i_commerce.global.security.jwt.dto.RefreshTokenPayload;
import com.example.i_commerce.global.security.jwt.dto.TokenPayload;
import com.example.i_commerce.global.security.jwt.entity.RefreshToken;
import com.example.i_commerce.global.security.jwt.repo.RefreshTokenRepository;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final DataEncryptor dataEncryptor;
    private final PasswordEncoder passwordEncoder;
    private final EmailHashEncoder emailHashEncoder;
    private final TokenHashEncoder tokenHashEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final SellerRepository sellerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginLogService loginLogService;
    private final RefreshTokenValidator refreshTokenValidator;
    private final BlacklistedTokenService blacklistedTokenService;
    private final RefreshTokenService refreshTokenService;

    //이벤트 처리용
    private final ApplicationEventPublisher eventPublisher;

    //측정용
    private final PerfTimer perfTimer;

    //회원 가입
    @Transactional
    public SignUpResponse signUp(MemberSignUpRequest dto) {
        String emailHash = emailHashEncoder.encode(dto.email());
        if (memberRepository.findByEmailHash(emailHash).isPresent()) {//이메일 중복시 예외처리
            throw new AppException(MemberErrorCode.DUPLICATED_EMAIL);
        }

        Member member = Member.builder()
            .emailHash(emailHash)
            .emailEncrypted(dataEncryptor.encrypt(dto.email()))
            .password(passwordEncoder.encode(dto.password()))
            .name(dataEncryptor.encrypt(dto.name()))
            .sex(dto.gender())
            .birthday(dataEncryptor.encrypt(dto.birthday()))
            .phoneNumber(dataEncryptor.encrypt(dto.phoneNumber()))
            .build();

        try {//같은 email로 요청 A, B 동시에 들어왔을 때 한쪽만 에러처리
            Member savedMember = memberRepository.saveAndFlush(member);
            return new SignUpResponse(
                savedMember.getId(),
                dataEncryptor.decrypt(savedMember.getEmailEncrypted())
            );
        } catch (DataIntegrityViolationException e) {
            throw new AppException(MemberErrorCode.DUPLICATED_EMAIL);
        }

    }

    // 로그인
//    public LoginResponse login(LoginRequest dto) {
//        String emailHash = emailHashEncoder.encode(dto.email());
//
//        Member member = memberRepository.findByEmailHash(emailHash)
//            .orElse(null);
//
//        if (member == null) {
//            eventPublisher.publishEvent(
//                new MemberLoginHistoryEvent(
//                    null,
//                    LoginResult.FAILURE,
//                    null,
//                    LocalDateTime.now(),
//                    LoginFailReason.INVALID_CREDENTIALS
//                )
//            );
//
//            throw new AppException(MemberErrorCode.USER_NOT_FOUND);
//        }
//
//        validateLoginStatus(member);
//        loginLogService.validateNotBlocked(emailHash);
//
//        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
//            eventPublisher.publishEvent(
//                new MemberLoginHistoryEvent(
//                    member.getId(),
//                    LoginResult.FAILURE,
//                    null,
//                    LocalDateTime.now(),
//                    LoginFailReason.PASSWORD_MISMATCH
//                )
//            );
//
//            // 실패 카운트/차단 판단은 동기 유지
//            loginLogService.userLoginFailedSequence(member.getEmailHash());
//
//            throw new AppException(MemberErrorCode.INVALID_PASSWORD);
//        }
//
//        Seller seller = null;
//
//        if (member.getIsSeller()) {
//            seller = sellerRepository.findById(member.getId())
//                .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND));
//        }
//
//        TokenPayload payload;
//
//        if (seller != null) {
//            payload = new TokenPayload(
//                PrincipalType.MEMBER,
//                member.getId(),
//                MemberType.SELLER,
//                member.getStatus(),
//                seller.getSellerStatus()
//            );
//        } else {
//            payload = new TokenPayload(
//                PrincipalType.MEMBER,
//                member.getId(),
//                MemberType.CUSTOMER,
//                member.getStatus(),
//                null
//            );
//        }
//
//        String accessToken = jwtTokenUtil.createToken(payload);
//
//        String tokenId = UUID.randomUUID().toString();
//
//        RefreshTokenPayload refreshTokenPayload = new RefreshTokenPayload(
//            PrincipalType.MEMBER,
//            member.getId(),
//            tokenId
//        );
//
//        String refreshToken = jwtTokenUtil.createRefreshToken(refreshTokenPayload);
//        String refreshTokenHash = tokenHashEncoder.encode(refreshToken);
//
//        RefreshToken savedRefreshToken = RefreshToken.create(
//            tokenId,
//            payload.principalType(),
//            payload.accountId(),
//            refreshTokenHash,
//            LocalDateTime.now().plusDays(7)
//        );
//
//        refreshTokenService.save(savedRefreshToken);
//
//        eventPublisher.publishEvent(
//            new MemberLoginHistoryEvent(
//                member.getId(),
//                LoginResult.SUCCESS,
//                null,
//                LocalDateTime.now(),
//                null
//            )
//        );
//
//        return new LoginResponse(
//            member.getId(),
//            dto.email(),
//            accessToken,
//            refreshToken
//        );
//    }

    //측정용 로그인
    public LoginResponse login(LoginRequest dto) {
        return perfTimer.record("member_login", "total", () -> {
            String emailHash = perfTimer.record("member_login", "email_hash", () ->
                emailHashEncoder.encode(dto.email())
            );

            Member member = perfTimer.record("member_login", "member_find_by_email_hash", () ->
                memberRepository.findByEmailHash(emailHash)
                    .orElse(null)
            );

            if (member == null) {
                perfTimer.record("member_login", "failure_event_publish_user_not_found", () ->
                    eventPublisher.publishEvent(
                        new MemberLoginHistoryEvent(
                            null,
                            LoginResult.FAILURE,
                            null,
                            LocalDateTime.now(),
                            LoginFailReason.INVALID_CREDENTIALS
                        )
                    )
                );

                throw new AppException(MemberErrorCode.USER_NOT_FOUND);
            }

            perfTimer.record("member_login", "status_validate", () ->
                validateLoginStatus(member)
            );

            perfTimer.record("member_login", "blacklist_validate", () ->
                loginLogService.validateNotBlocked(emailHash)
            );

            Boolean passwordMatched = perfTimer.record("member_login", "password_match", () ->
                passwordEncoder.matches(dto.password(), member.getPassword())
            );

            if (!passwordMatched) {
                perfTimer.record("member_login", "failure_event_publish_password_mismatch", () ->
                    eventPublisher.publishEvent(
                        new MemberLoginHistoryEvent(
                            member.getId(),
                            LoginResult.FAILURE,
                            null,
                            LocalDateTime.now(),
                            LoginFailReason.PASSWORD_MISMATCH
                        )
                    )
                );

                // 실패 카운트/차단 판단은 동기 유지
                perfTimer.record("member_login", "failed_login_sequence", () ->
                    loginLogService.userLoginFailedSequence(member.getEmailHash())
                );

                throw new AppException(MemberErrorCode.INVALID_PASSWORD);
            }

            Seller seller = null;

            if (member.getIsSeller()) {
                seller = perfTimer.record("member_login", "seller_find", () ->
                    sellerRepository.findById(member.getId())
                        .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND))
                );
            }

            Seller finalSeller = seller;

            TokenPayload payload = perfTimer.record("member_login", "access_token_payload_create",
                () -> {
                    if (finalSeller != null) {
                        return new TokenPayload(
                            PrincipalType.MEMBER,
                            member.getId(),
                            MemberType.SELLER,
                            member.getStatus(),
                            finalSeller.getSellerStatus()
                        );
                    }

                    return new TokenPayload(
                        PrincipalType.MEMBER,
                        member.getId(),
                        MemberType.CUSTOMER,
                        member.getStatus(),
                        null
                    );
                });

            String accessToken = perfTimer.record("member_login", "access_token_create", () ->
                jwtTokenUtil.createToken(payload)
            );

            String tokenId = perfTimer.record("member_login", "refresh_token_id_create", () ->
                UUID.randomUUID().toString()
            );

            RefreshTokenPayload refreshTokenPayload = perfTimer.record("member_login",
                "refresh_token_payload_create", () ->
                    new RefreshTokenPayload(
                        PrincipalType.MEMBER,
                        member.getId(),
                        tokenId
                    )
            );

            String refreshToken = perfTimer.record("member_login", "refresh_token_create", () ->
                jwtTokenUtil.createRefreshToken(refreshTokenPayload)
            );

            String refreshTokenHash = perfTimer.record("member_login", "refresh_token_hash", () ->
                tokenHashEncoder.encode(refreshToken)
            );

            RefreshToken savedRefreshToken = perfTimer.record("member_login",
                "refresh_token_entity_create", () ->
                    RefreshToken.create(
                        tokenId,
                        payload.principalType(),
                        payload.accountId(),
                        refreshTokenHash,
                        LocalDateTime.now().plusDays(7)
                    )
            );

            perfTimer.record("member_login", "refresh_token_save", () ->
                refreshTokenService.save(savedRefreshToken)
            );

            perfTimer.record("member_login", "success_event_publish", () ->
                eventPublisher.publishEvent(
                    new MemberLoginHistoryEvent(
                        member.getId(),
                        LoginResult.SUCCESS,
                        null,
                        LocalDateTime.now(),
                        null
                    )
                )
            );

            return perfTimer.record("member_login", "response_create", () ->
                new LoginResponse(
                    member.getId(),
                    dto.email(),
                    accessToken,
                    refreshToken
                )
            );
        });
    }

    //토큰 재발급
    @Transactional
    public TokenReissueResponse reissue(TokenReissueRequest request) {

        RefreshTokenPayload refreshPayload = refreshTokenValidator.validate(
            request.refreshToken(),
            PrincipalType.MEMBER
        );

        TokenPayload accessPayload = createMemberAccessPayload(refreshPayload.accountId());

        String accessToken = jwtTokenUtil.createToken(accessPayload);

        return new TokenReissueResponse(accessToken);
    }

    private TokenPayload createMemberAccessPayload(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        validateMemberCanReissue(member);

        SellerStatus sellerStatus = null;

        if (member.getIsSeller() == true) {
            Seller seller = sellerRepository.findById(member.getId())
                .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND));

            sellerStatus = seller.getSellerStatus();
        }

        return new TokenPayload(
            PrincipalType.MEMBER,
            member.getId(),
            member.getRole(),
            member.getStatus(),
            sellerStatus
        );
    }

    private void validateMemberCanReissue(Member member) {
        if (member.getDeletedAt() != null) {
            throw new AppException(MemberErrorCode.WITHDRAWN_MEMBER);
        }

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AppException(MemberErrorCode.WITHDRAWN_MEMBER);
        }

        if (member.getStatus() == MemberStatus.INACTIVE) {
            throw new AppException(MemberErrorCode.INACTIVE_MEMBER);
        }
    }

    //    로그아웃
    @Transactional
    public void logout(String authorization, TokenLogoutRequest request) {
        String accessToken = authorization.substring(7);
        blacklistedTokenService.logout(accessToken);

        RefreshToken savedToken = refreshTokenValidator.validateAndGetToken(
            request.refreshToken(),
            PrincipalType.MEMBER
        );

        refreshTokenRepository.delete(savedToken);
    }

    //    계정 찾기
    @Transactional(readOnly = true)
    public AccountFindEmailResponse findEmail(AccountFindEmailRequest dto) {

        Member member = memberRepository.findAllByDeletedAtIsNull()
            .stream()
            .filter(m -> isSamePersonalInfo(m, dto.name(), dto.phoneNumber()))
            .findFirst()
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        validateAvailableMember(member);

        String email = dataEncryptor.decrypt(member.getEmailEncrypted());

        return new AccountFindEmailResponse(email);
    }

    //    비밀번호 찾기
    @Transactional
    public void findPassword(PasswordFindRequest dto) {

        String emailHash = emailHashEncoder.encode(dto.email());

        Member member = memberRepository.findByEmailHashAndDeletedAtIsNull(emailHash)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        validateAvailableMember(member);

        if (!isSamePersonalInfo(member, dto.name(), dto.phoneNumber())) {
            throw new AppException(MemberErrorCode.INVALID_INPUT_VALUE);
        }

        String encodedPassword = passwordEncoder.encode(dto.newPassword());

        member.changePassword(encodedPassword);
    }

    private boolean isSamePersonalInfo(Member member, String name, String phoneNumber) {
        String decryptedName = dataEncryptor.decrypt(member.getName());
        String decryptedPhoneNumber = dataEncryptor.decrypt(member.getPhoneNumber());

        return decryptedName.equals(name)
            && decryptedPhoneNumber.equals(phoneNumber);
    }

    private void validateAvailableMember(Member member) {
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AppException(MemberErrorCode.WITHDRAWN_MEMBER);
        }

        if (member.getStatus() == MemberStatus.INACTIVE) {
            throw new AppException(MemberErrorCode.INACTIVE_MEMBER);
        }
    }

    //    비밀번호 재설정
    @Transactional
    public void resetPassword(Long memberId, PasswordResetRequest dto) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.oldPassword(), member.getPassword())) {
            throw new AppException(MemberErrorCode.INVALID_PASSWORD);
        }

        member.changePassword(passwordEncoder.encode(dto.newPassword()));
    }

    //    회원 탈퇴
    @Transactional
    public void withdraw(Long memberId, WithDrawRequest dto) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new AppException(MemberErrorCode.INVALID_PASSWORD);
        }

        member.delete();
    }

    //    정보조회
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        return UserInfoResponse.from(member, dataEncryptor);
    }

    //    회원정보수정
    @Transactional
    public UserInfoResponse updateMyInfo(Long memberId, UserUpdateRequest request) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        member.updateInfo(
            dataEncryptor.encrypt(request.name()),
            dataEncryptor.encrypt(request.phoneNumber()),
            request.gender(),
            dataEncryptor.encrypt(request.birthday())
        );

        return UserInfoResponse.from(member, dataEncryptor);
    }

    private void validateLoginStatus(Member member) {
        switch (member.getStatus()) {
            case INACTIVE -> throw new AppException(MemberErrorCode.INACTIVE_MEMBER);
            case WITHDRAWN -> throw new AppException(MemberErrorCode.WITHDRAWN_MEMBER);
        }
    }
}
