package com.example.i_commerce.domain.member.service.seller;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.service.auth.dto.WithDrawRequest;
import com.example.i_commerce.domain.member.service.seller.dto.SellerInfoResponse;
import com.example.i_commerce.domain.member.service.seller.dto.SellerRequest;
import com.example.i_commerce.domain.member.service.seller.dto.SellerResponse;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final MemberRepository memberRepository;
    private final DataEncryptor dataEncryptor;
    private final PasswordEncoder passwordEncoder;

    //판매자 신청 서비스
    @Transactional
    public SellerResponse applyForSeller(
        Long memberId,
        SellerRequest dto
    ) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        Seller seller = sellerRepository.findById(memberId)
            .map(existingSeller -> {//seller등록이 있을 때 상태가 WITHDRAW면 업데이트, 아니면 예외
                if (existingSeller.getSellerStatus() != SellerStatus.WITHDRAW) {
                    throw new AppException(MemberErrorCode.ALREADY_APPLIED_SELLER);
                }

                existingSeller.update(
                    dto.businessName(),
                    dto.businessNumber(),
                    dto.mailOrderRegistrationNumber(),
                    dto.ownerName(),
                    dto.phoneNumber(),
                    dataEncryptor.encrypt(dto.bankName()),
                    dataEncryptor.encrypt(dto.bankAccount()),
                    dataEncryptor.encrypt(dto.depositorName())
                );
                existingSeller.changeStatus(SellerStatus.PENDING);
                existingSeller.restore();
                return existingSeller;
            })
            .orElseGet(() -> Seller.builder()//seller 등록이 없으면 새로 생성
                .member(member)
                .businessName(dto.businessName())
                .businessNumber(dto.businessNumber())
                .mailOrderRegistrationNumber(dto.mailOrderRegistrationNumber())
                .ownerName(dto.ownerName())
                .phoneNumber(dto.phoneNumber())
                .sellerStatus(SellerStatus.PENDING)
                .bankName(dataEncryptor.encrypt(dto.bankName()))
                .bankAccount(dataEncryptor.encrypt(dto.bankAccount()))
                .depositorName(dataEncryptor.encrypt(dto.depositorName()))
                .build()
            );

        Seller savedSeller = sellerRepository.save(seller);
        member.isSeller();

        return new SellerResponse(
            savedSeller.getId(),
            savedSeller.getCreatedAt()
        );
    }

    //판매자 정보 조회
    @Transactional(readOnly = true)
    public SellerInfoResponse getSellerInfo(Long memberId) {
        Seller seller = sellerRepository.findById(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND));

        return new SellerInfoResponse(
            seller.getBusinessName(),
            seller.getBusinessNumber(),
            seller.getMailOrderRegistrationNumber(),
            seller.getOwnerName(),
            seller.getPhoneNumber(),
            seller.getSellerStatus(),
            seller.getApprovedAt(),
            dataEncryptor.decrypt(seller.getBankName()),
            dataEncryptor.decrypt(seller.getBankAccount()),
            dataEncryptor.decrypt(seller.getDepositorName())
        );
    }

    //판매자 정보 수정
    @Transactional
    public SellerResponse updateSeller(
        Long memberId,
        SellerRequest dto
    ) {
        Seller seller = sellerRepository.findById(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND));

        seller.update(
            dto.businessName(),
            dto.businessNumber(),
            dto.mailOrderRegistrationNumber(),
            dto.ownerName(),
            dto.phoneNumber(),
            dataEncryptor.encrypt(dto.bankName()),
            dataEncryptor.encrypt(dto.bankAccount()),
            dataEncryptor.encrypt(dto.depositorName())
        );

        Seller savedSeller = sellerRepository.save(seller);

        return new SellerResponse(savedSeller.getId(), savedSeller.getUpdatedAt());
    }

    @Transactional
    public void deleteSeller(Long memberId, WithDrawRequest dto) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new AppException(MemberErrorCode.INVALID_PASSWORD);
        }

        Seller seller = sellerRepository.findByIdAndDeletedAtIsNull(member.getId())
            .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND));

        seller.delete();

        //연결된 상점들도 정지되도록 해야함, 즉시 로그아웃 되게 해야함
    }
}
