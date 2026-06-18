package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductStoreFixture {

    private final DataEncryptor dataEncryptor;
    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;
    private final StoreRepository storeRepository;
    
    public Member createMember() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return memberRepository.save(Member.builder()
            .name(dataEncryptor.encrypt("테스트회원"))
            .phoneNumber(dataEncryptor.encrypt("010-1234-5678"))
            .emailHash("hashedEmail" + uniqueId)
            .emailEncrypted(dataEncryptor.encrypt("test@example.com"))
            .password("password")
            .sex(Gender.MALE)
            .birthday(dataEncryptor.encrypt("20431123"))
            .build());
    }

    public Seller createSeller(Member member) {
        return sellerRepository.save(Seller.builder()
            .member(member)
            .businessName("테스트 사업체")
            .businessNumber("123-45-67890")
            .mailOrderRegistrationNumber("2024-서울-0001")
            .ownerName("홍길동")
            .phoneNumber("01012345678")
            .sellerStatus(SellerStatus.APPROVED)
            .bankName("신한은행".getBytes())
            .bankAccount("110-123-456789".getBytes())
            .depositorName("홍길동".getBytes())
            .build());
    }

    public Store createStore(Long sellerId) {
        return storeRepository.save(Store.builder()
            .sellerId(sellerId)
            .storeName("테스트 상점")
            .phoneNumber("02-1234-5678")
            .storeStatus(StoreStatus.OPEN)
            .build());
    }

}
