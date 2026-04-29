package com.example.i_commerce.domain.member.service;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.repo.MemberRepository;
import com.example.i_commerce.domain.member.service.dto.MemberChatInfo;
import com.example.i_commerce.domain.member.service.dto.MemberNotificationInfo;
import com.example.i_commerce.domain.member.service.dto.MemberOrderInfo;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
import com.example.i_commerce.global.security.tools.DataEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final DataEncryptor dataEncryptor;

    @Transactional(readOnly = true)
    public MemberOrderInfo getMemberOrderInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        return new MemberOrderInfo(
            member.getId(),
            member.getEmail(),
            dataEncryptor.decrypt(member.getName()),
            dataEncryptor.decrypt(member.getPhoneNumber())
        );
    }

    @Transactional(readOnly = true)
    public MemberChatInfo getMemberChatInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        return new MemberChatInfo(
            member.getId(),
            dataEncryptor.decrypt(member.getName())
        );
    }

    @Transactional(readOnly = true)
    public MemberNotificationInfo getMemberNotificationInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        return new MemberNotificationInfo(
            member.getId(),
            member.getEmail(),
            dataEncryptor.decrypt(member.getPhoneNumber())
        );
    }
}
