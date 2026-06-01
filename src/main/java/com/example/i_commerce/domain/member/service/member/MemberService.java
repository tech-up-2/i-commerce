package com.example.i_commerce.domain.member.service.member;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.service.member.dto.MemberChatInfo;
import com.example.i_commerce.domain.member.service.member.dto.MemberNotificationInfo;
import com.example.i_commerce.domain.member.service.member.dto.MemberOrderInfo;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;
    private final AdminRepository adminRepository;
    private final DataEncryptor dataEncryptor;

    @Transactional(readOnly = true)
    public MemberOrderInfo getMemberOrderInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        return new MemberOrderInfo(
            member.getId(),
            dataEncryptor.decrypt(member.getEmailEncrypted()),
            dataEncryptor.decrypt(member.getName()),
            dataEncryptor.decrypt(member.getPhoneNumber())
        );
    }

    // --- 채팅 ---
    @Transactional(readOnly = true)
    public MemberChatInfo getMemberChatInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        return new MemberChatInfo(
            member.getId(),
            dataEncryptor.decrypt(member.getName())
        );
    }

    @Transactional(readOnly = true)
    public MemberChatInfo getSellerChatInfo(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND));

        return new MemberChatInfo(
            seller.getId(),
            seller.getBusinessName()
        );
    }

    @Transactional(readOnly = true)
    public MemberChatInfo getAdminChatInfo(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new AppException(MemberErrorCode.ADMIN_NOT_FOUND));

        return new MemberChatInfo(
            admin.getId(),
            dataEncryptor.decrypt(admin.getName())
        );
    }
    // -------

    @Transactional(readOnly = true)
    public MemberNotificationInfo getMemberNotificationInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        return new MemberNotificationInfo(
            member.getId(),
            dataEncryptor.decrypt(member.getEmailEncrypted()),
            dataEncryptor.decrypt(member.getPhoneNumber())
        );
    }


}
