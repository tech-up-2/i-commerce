package com.example.i_commerce.domain.member.service;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.service.dto.MemberSignUpRequest;
import com.example.i_commerce.domain.member.service.dto.SignUpResponse;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
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

    @Transactional
    public SignUpResponse signUp(MemberSignUpRequest dto) {
        if (memberRepository.findByEmailHash(dto.email()).isPresent()) {//이메일 중복시 예외처리
            throw new AppException(ErrorCode.DUPLICATED_EMAIL);
        }

        Member member = Member.builder()
            .emailHash(emailHashEncoder.encode(dto.email()))
            .emailEncrypted(dataEncryptor.encrypt(dto.email()))
            .password(passwordEncoder.encode(dto.password()))
            .name(dataEncryptor.encrypt(dto.name()))
            .sex(dto.gender())
            .birthday(dataEncryptor.encrypt(dto.birthday()))
            .phoneNumber(dataEncryptor.encrypt(dto.phoneNumber()))
            .build();

        try {//같은 email로 요청 A, B 동시에 들어왔을 때 한쪽만 에러처리
            Member savedMember = memberRepository.saveAndFlush(member);
            return new SignUpResponse(savedMember.getId(), savedMember.getEmailHash());
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.DUPLICATED_EMAIL);
        }

    }
}
