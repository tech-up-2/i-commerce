package com.example.i_commerce.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;

import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.service.auth.dto.WithDrawRequest;
import com.example.i_commerce.domain.member.service.seller.SellerService;
import com.example.i_commerce.domain.member.service.seller.dto.SellerInfoResponse;
import com.example.i_commerce.domain.member.service.seller.dto.SellerRequest;
import com.example.i_commerce.domain.member.service.seller.dto.SellerResponse;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.domain.testtools.IntegrationTestSupport;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
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
public class SellerServiceTest extends IntegrationTestSupport {

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

    private CustomUserPrincipal member;

    private CustomUserPrincipal seller;

    @BeforeEach
    void setUp() {
        member = loginAsActiveMaleMember();
    }

    @BeforeEach
    void setup() {
        seller = loginAsApprovedSeller();
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
        CustomUserPrincipal customUserPrincipal = loginAsWithdrawSellerAfterApproval();

        SellerRequest request = createSellerRequest("새상호");

        // when
        SellerResponse response = sellerService.applyForSeller(customUserPrincipal.getId(),
            request);

        // then
        Seller seller = sellerRepository.findById(customUserPrincipal.getId()).orElseThrow();

        assertThat(response.sellerId()).isEqualTo(customUserPrincipal.getId());
        assertThat(seller.getBusinessName()).isEqualTo(request.businessName());
        assertThat(seller.getSellerStatus()).isEqualTo(SellerStatus.PENDING);
    }

    @Test
    @DisplayName("판매자 신청 실패 - PENDING 상태면 이미 신청된 판매자로 판단한다")
    void applyForSeller_fail_pendingSeller() {
        // given
        CustomUserPrincipal customUserPrincipal = loginAsPendingSeller();

        SellerRequest request = createSellerRequest("kt마켓");

        // when & then
        assertThatThrownBy(() -> sellerService.applyForSeller(customUserPrincipal.getId(), request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("판매자 신청 실패 - APPROVED 상태면 이미 신청된 판매자로 판단한다")
    void applyForSeller_fail_approvedSeller() {
        // given
        CustomUserPrincipal customUserPrincipal = loginAsApprovedSeller();

        SellerRequest request = createSellerRequest("kt마켓");

        // when & then
        assertThatThrownBy(() -> sellerService.applyForSeller(customUserPrincipal.getId(), request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("판매자 신청 실패 - BLOCKED 상태면 이미 신청된 판매자로 판단한다")
    void applyForSeller_fail_blockedSeller() {
        // given
        CustomUserPrincipal customUserPrincipal = loginAsBlockedSellerAfterApproval();

        SellerRequest request = createSellerRequest("kt마켓");

        // when & then
        assertThatThrownBy(() -> sellerService.applyForSeller(customUserPrincipal.getId(), request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("판매자 신청 실패 - 존재하지 않는 회원")
    void applyForSeller_fail_membernotfound() {

        SellerRequest request = createSellerRequest("kt마켓");

        // when & then
        assertThatThrownBy(() -> sellerService.applyForSeller(null, request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("판매자 정보 조회 성공")
    void getSellerInfo_success() {
        // given
        CustomUserPrincipal customUserPrincipal = loginAsApprovedSeller();

        // when
        SellerInfoResponse response = sellerService.getSellerInfo(customUserPrincipal.getId());

        // then
        assertThat(response.businessName()).isEqualTo(
            sellerRepository.findById(customUserPrincipal.getId()).get().getBusinessName());
        assertThat(response.businessNumber()).isEqualTo(
            sellerRepository.findById(customUserPrincipal.getId()).get().getBusinessNumber());
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
        CustomUserPrincipal customUserPrincipal = loginAsApprovedSeller();

        SellerRequest request = createSellerRequest("수정상호");

        // when
        SellerResponse response = sellerService.updateSeller(customUserPrincipal.getId(), request);

        // then
        Seller seller = sellerRepository.findById(customUserPrincipal.getId()).orElseThrow();

        assertThat(response.sellerId()).isEqualTo(customUserPrincipal.getId());
        assertThat(seller.getBusinessName()).isEqualTo("수정상호");
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

    @Test
    @DisplayName("판매자 탈퇴 테스트")
    void deleteSeller_success() {
        CustomUserPrincipal principal = loginAsApprovedSeller();

        WithDrawRequest request = new WithDrawRequest("password123!");

        sellerService.deleteSeller(principal.getId(), request);

        Seller seller = sellerRepository.findById(principal.getId()).orElseThrow();

        assertThat(seller.getSellerStatus()).isEqualTo(SellerStatus.WITHDRAW);
        assertThat(seller.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("판매자 탈퇴 테스트 - 판매자 없음")
    void deleteSeller_fail_sellernotfound() {
        WithDrawRequest request = new WithDrawRequest("password123!");

        AppException exception = assertThrows(AppException.class,
            () -> sellerService.deleteSeller(member.getId(), request));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.SELLER_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자 탈퇴 실패 - 이미 탈퇴한 판매자")
    void deleteSeller_fail_alreadyDeletedSeller() {
        // given
        WithDrawRequest request = new WithDrawRequest("password123!");

        sellerService.deleteSeller(seller.getId(), request);

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> sellerService.deleteSeller(member.getId(), request)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.SELLER_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자 탈퇴 실패 - 존재하지 않는 회원")
    void deleteSeller_fail_membernotfound() {
        // given
        WithDrawRequest request = new WithDrawRequest("password123!");

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> sellerService.deleteSeller(null, request)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자 탈퇴 실패 - 비밀번호 불일치")
    void deleteSeller_fail_invalidPassword() {
        WithDrawRequest request = new WithDrawRequest("wrongPassword");

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> sellerService.deleteSeller(seller.getId(), request)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.INVALID_PASSWORD);
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
            .member(memberRepository.findById(memberId).orElseThrow())
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
