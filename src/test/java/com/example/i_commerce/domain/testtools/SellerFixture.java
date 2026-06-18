package com.example.i_commerce.domain.testtools;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SellerFixture {

    private static final BankName[] BANKS = BankName.values();

    public static Seller createSeller(
        Member member,
        SellerStatus status,
        boolean approvedAt,
        DataEncryptor dataEncryptor
    ) {
        String businessName = status.name().toLowerCase() + "-" + UUID.randomUUID() + "_Store";

        LocalDateTime localDateTime;

        if (approvedAt) {
            localDateTime = LocalDateTime.now();
        } else {
            localDateTime = null;
        }

        return Seller.builder()
            .member(member)
            .businessName(businessName)
            .businessNumber("123-54-67890")
            .mailOrderRegistrationNumber("2025-서울용산-01075")
            .ownerName(dataEncryptor.decrypt(member.getName()))
            .phoneNumber("021234567")
            .sellerStatus(status)
            .approvedAt(localDateTime)
            .bankName(dataEncryptor.encrypt(randomBankName() + "은행"))
            .bankAccount(dataEncryptor.encrypt("1234567890"))
            .depositorName(dataEncryptor.encrypt(dataEncryptor.decrypt(member.getName())))
            .build();
    }

    private static BankName randomBankName() {
        return BANKS[ThreadLocalRandom.current().nextInt(BANKS.length)];
    }
}