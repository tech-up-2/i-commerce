package com.example.i_commerce.domain.member.service;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.MemberErrorCode;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.service.dto.LoginRequest;
import com.example.i_commerce.domain.member.service.dto.LoginResponse;
import com.example.i_commerce.domain.member.service.dto.MemberSignUpRequest;
import com.example.i_commerce.domain.member.service.dto.SignUpResponse;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
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

    @Transactional
    public SignUpResponse signUp(MemberSignUpRequest dto) {
        String emailHash = emailHashEncoder.encode(dto.email());
        if (memberRepository.findByEmailHash(emailHash).isPresent()) {//이메일 중복시 예외처리
            throw new AppException(ErrorCode.DUPLICATED_EMAIL);
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
            throw new AppException(ErrorCode.DUPLICATED_EMAIL);
        }

    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest dto) {
        Member member = memberRepository.findByEmailHash(emailHashEncoder.encode(dto.email()))
            .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        if (member.getStatus() == MemberStatus.INACTIVE ||
            member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AppException(MemberErrorCode.INVALID_MEMBER);
        }

        String email = dataEncryptor.decrypt(member.getEmailEncrypted());

        String accessToken = jwtTokenUtil.createToken(
            member.getId(),
            email,
            member.getRole(),
            member.getStatus()
        );

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
