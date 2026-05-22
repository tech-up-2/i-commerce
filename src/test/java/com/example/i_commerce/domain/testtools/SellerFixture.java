package com.example.i_commerce.domain.testtools;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import java.time.LocalDateTime;
import java.util.UUID;

public class SellerFixture {

    public static Seller createSeller(
        Member member, SellerStatus status,DataEncryptor dataEncryptor) {
        String businessName = status.name().toLowerCase() + "-" + UUID.randomUUID() + "_Store";

        LocalDateTime localDateTime;

        if(status.equals(SellerStatus.APPROVED) || ){
            localDateTime = LocalDateTime.now();
        } else{
            localDateTime = null;
        }

        return Seller.builder()
            .member(member)
            .businessName("Pendding_Store")
            .businessNumber("1235467890")
            .mailOrderRegistrationNumber("2025-서울용산-01075")
            .ownerName("홍길동")
            .phoneNumber("021234567")
            .sellerStatus(status)
            .approvedAt(localDateTime)
            .bankName(dataEncryptor.encrypt("한국은행"))
            .bankAccount(dataEncryptor.encrypt("1234567890"))
            .depositorName(dataEncryptor.encrypt("홍길동"))
            .build();
    }


}
