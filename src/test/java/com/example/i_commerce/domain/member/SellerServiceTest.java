package com.example.i_commerce.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.service.seller.SellerService;
import com.example.i_commerce.domain.member.service.seller.dto.SellerInfoResponse;
import com.example.i_commerce.domain.member.service.seller.dto.SellerRequest;
import com.example.i_commerce.domain.member.service.seller.dto.SellerResponse;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.global.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "file:.env")
public class SellerServiceTest {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailHashEncoder emailHashEncoder;

    @Autowired
    private DataEncryptor dataEncryptor;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
            MemberFixture.createMember(
                passwordEncoder,
                emailHashEncoder,
                dataEncryptor
            )
        );
    }

    @Test
    @DisplayName("판매자 신청 성공 - 기존 판매자 정보가 없으면 신규 생성된다")
    void applyForSeller_success_newSeller() {
        Long memberId = member.getId();
        SellerRequest request = createSellerRequest("kt마켓");

        SellerResponse response = sellerService.applyForSeller(memberId, request);

        Seller seller = sellerRepository.findById(memberId).orElseThrow();

        assertThat(response.sellerId()).isEqualTo(memberId);
        assertThat(seller.getId()).isEqualTo(memberId);
        assertThat(seller.getBusinessName()).isEqualTo("kt마켓");
        assertThat(seller.getSellerStatus()).isEqualTo(SellerStatus.PENDING);
    }

    @Test
    @DisplayName("판매자 재신청 성공 - WITHDRAW 상태면 정보를 덮어쓰고 PENDING 상태가 된다")
    void applyForSeller_success_withdrawSeller() {
        // given
        Long memberId = member.getId();

        Seller withdrawSeller = Seller.builder()
            .member(member)
            .businessName("기존상호")
            .businessNumber("0000000000")
            .mailOrderRegistrationNumber("기존신고번호")
            .ownerName("기존대표")
            .phoneNumber("01000000000")
            .sellerStatus(SellerStatus.WITHDRAW)
            .bankName(dataEncryptor.encrypt("기존은행"))
            .bankAccount(dataEncryptor.encrypt("0000000000"))
            .depositorName(dataEncryptor.encrypt("기존예금주"))
            .build();

        sellerRepository.save(withdrawSeller);

        SellerRequest request = createSellerRequest("새상호");

        // when
        SellerResponse response = sellerService.applyForSeller(memberId, request);

        // then
        Seller seller = sellerRepository.findById(memberId).orElseThrow();

        assertThat(response.sellerId()).isEqualTo(memberId);
        assertThat(seller.getBusinessName()).isEqualTo("새상호");
        assertThat(seller.getBusinessNumber()).isEqualTo("1234567890");
        assertThat(seller.getSellerStatus()).isEqualTo(SellerStatus.PENDING);
    }

    @Test
    @DisplayName("판매자 신청 실패 - PENDING 상태면 이미 신청된 판매자로 판단한다")
    void applyForSeller_fail_pendingSeller() {
        // given
        Long memberId = member.getId();
        sellerRepository.save(createSeller(memberId, SellerStatus.PENDING));

        SellerRequest request = createSellerRequest("kt마켓");

        // when & then
        assertThatThrownBy(() -> sellerService.applyForSeller(memberId, request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("판매자 신청 실패 - APPROVED 상태면 이미 신청된 판매자로 판단한다")
    void applyForSeller_fail_approvedSeller() {
        // given
        Long memberId = member.getId();
        sellerRepository.save(createSeller(memberId, SellerStatus.APPROVED));

        SellerRequest request = createSellerRequest("kt마켓");

        // when & then
        assertThatThrownBy(() -> sellerService.applyForSeller(memberId, request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("판매자 신청 실패 - BLOCKED 상태면 이미 신청된 판매자로 판단한다")
    void applyForSeller_fail_blockedSeller() {
        // given
        Long memberId = member.getId();
        sellerRepository.save(createSeller(memberId, SellerStatus.BLOCKED));

        SellerRequest request = createSellerRequest("kt마켓");

        // when & then
        assertThatThrownBy(() -> sellerService.applyForSeller(memberId, request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("판매자 정보 조회 성공")
    void getSellerInfo_success() {
        // given
        Long memberId = member.getId();
        sellerRepository.save(createSeller(memberId, SellerStatus.APPROVED));

        // when
        SellerInfoResponse response = sellerService.getSellerInfo(memberId);

        // then
        assertThat(response.businessName()).isEqualTo("kt마켓");
        assertThat(response.businessNumber()).isEqualTo("1234567890");
        assertThat(response.sellerStatus()).isEqualTo(SellerStatus.APPROVED);
    }

    @Test
    @DisplayName("판매자 정보 조회 실패 - 판매자 정보가 없으면 예외 발생")
    void getSellerInfo_fail_notFound() {
        // given
        Long memberId = member.getId();

        // when & then
        assertThatThrownBy(() -> sellerService.getSellerInfo(memberId))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("판매자 정보 수정 성공")
    void updateSeller_success() {
        // given
        Long memberId = member.getId();
        sellerRepository.save(createSeller(memberId, SellerStatus.APPROVED));

        SellerRequest request = createSellerRequest("수정상호");

        // when
        SellerResponse response = sellerService.updateSeller(memberId, request);

        // then
        Seller seller = sellerRepository.findById(memberId).orElseThrow();

        assertThat(response.sellerId()).isEqualTo(memberId);
        assertThat(seller.getBusinessName()).isEqualTo("수정상호");
        assertThat(seller.getBusinessNumber()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("판매자 정보 수정 실패 - 판매자 정보가 없으면 예외 발생")
    void updateSeller_fail_notFound() {
        // given
        Long memberId = member.getId();
        SellerRequest request = createSellerRequest("수정상호");

        // when & then
        assertThatThrownBy(() -> sellerService.updateSeller(memberId, request))
            .isInstanceOf(AppException.class);
    }

    private SellerRequest createSellerRequest(String businessName) {
        return new SellerRequest(
            businessName,
            "1234567890",
            "2025-서울용산-01075",
            "홍길동",
            "01012345678",
            "국민은행",
            "1234567890",
            "홍길동"
        );
    }

    private Seller createSeller(Long memberId, SellerStatus status) {
        return Seller.builder()
            .member(member)
            .businessName("kt마켓")
            .businessNumber("1234567890")
            .mailOrderRegistrationNumber("2025-서울용산-01075")
            .ownerName("홍길동")
            .phoneNumber("01012345678")
            .sellerStatus(status)
            .bankName(dataEncryptor.encrypt("국민은행"))
            .bankAccount(dataEncryptor.encrypt("1234567890"))
            .depositorName(dataEncryptor.encrypt("홍길동"))
            .build();
    }
}
