package com.example.i_commerce.domain.member.service.auth;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.service.auth.dto.LoginRequest;
import com.example.i_commerce.domain.member.service.auth.dto.LoginResponse;
import com.example.i_commerce.domain.member.service.auth.dto.SignUpResponse;
import com.example.i_commerce.domain.member.service.member.dto.MemberSignUpRequest;
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

    private void validateLoginStatus(Member member) {
        switch (member.getStatus()) {
            case INACTIVE -> throw new AppException(MemberErrorCode.INACTIVE_MEMBER);
            case WITHDRAWN -> throw new AppException(MemberErrorCode.WITHDRAWN_MEMBER);
        }
    }
}
