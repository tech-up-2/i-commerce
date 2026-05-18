package com.example.i_commerce.domain.member.service.auth;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
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
import com.example.i_commerce.domain.member.service.auth.dto.UserInfoResponse;
import com.example.i_commerce.domain.member.service.auth.dto.UserUpdateRequest;
import com.example.i_commerce.domain.member.service.auth.dto.WithDrawRequest;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.TokenPayload;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import lombok.RequiredArgsConstructor;
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
    private final JwtTokenUtil jwtTokenUtil;
    private final SellerRepository sellerRepository;

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

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest dto) {
        Member member = memberRepository.findByEmailHash(emailHashEncoder.encode(dto.email()))
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new AppException(MemberErrorCode.INVALID_PASSWORD);
        }

        validateLoginStatus(member);// status상태 검즘

        String email = dataEncryptor.decrypt(member.getEmailEncrypted());

        TokenPayload payload;

        Seller seller = null;

        if (member.getIsSeller()) {
            seller = sellerRepository.findById(member.getId())
                .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND));
        }

        if (seller != null) {
            payload = new TokenPayload(
                PrincipalType.MEMBER,
                member.getId(),
                email,
                MemberType.SELLER,
                member.getStatus(),
                seller.getSellerStatus()
            );
        } else {
            payload = new TokenPayload(
                PrincipalType.MEMBER,
                member.getId(),
                email,
                MemberType.CUSTOMER,
                member.getStatus(),
                null
            );
        }

        String accessToken = jwtTokenUtil.createToken(payload);

        return new LoginResponse(
            member.getId(),
            email,
            accessToken
        );
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
