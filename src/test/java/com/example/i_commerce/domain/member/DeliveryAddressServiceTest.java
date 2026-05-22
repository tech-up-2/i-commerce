package com.example.i_commerce.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.DeliveryAddressRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.service.delivery.DeliveryAddressService;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressRequest;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressResponse;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Optional;
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
class DeliveryAddressServiceTest {

    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

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
    @DisplayName("배송지 등록 성공")
    void createDeliveryAddress_success() {

        // given
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "집",
            "홍길동",
            "010-1234-5678",
            "12345",
            "서울특별시 강남구 테헤란로",
            null,
            "101동 1001호",
            null,
            true,
            "문 앞에 놔주세요"
        );

        // when
        DeliveryAddressResponse response =
            deliveryAddressService.createNewAddress(
                request,
                member.getId()
            );

        // then
        assertThat(response.label()).isEqualTo("집");
        assertThat(response.recipientName()).isEqualTo("홍길동");
        assertThat(response.isDefault()).isTrue();

        List<DeliveryAddress> addresses =
            deliveryAddressRepository.findByMemberIdOrderByIsDefaultDescCreatedAtDesc(
                member.getId()
            );

        assertThat(addresses).hasSize(1);
    }

    @Test
    @DisplayName("첫 배송지는 자동으로 기본 배송지가 된다")
    void firstAddressBecomesDefaultAutomatically() {

        // given
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "회사",
            "홍길동",
            "010-1111-2222",
            "54321",
            "부산광역시 중구",
            null,
            "201호",
            null,
            false,
            null
        );

        // when
        DeliveryAddressResponse response =
            deliveryAddressService.createNewAddress(
                request,
                member.getId()
            );

        // then
        assertThat(response.isDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 변경 시 기존 기본 배송지는 해제된다")
    void changeDefaultAddress() {

        // given
        DeliveryAddressRequest first = new DeliveryAddressRequest(
            "집",
            "홍길동",
            "01012345678",
            "11111",
            "서울시",
            null,
            "101호",
            null,
            true,
            null
        );

        DeliveryAddressRequest second = new DeliveryAddressRequest(
            "회사",
            "홍길동",
            "01099999999",
            "22222",
            "부산시",
            null,
            "202호",
            null,
            true,
            null
        );

        // when
        deliveryAddressService.createNewAddress(first, member.getId());

        deliveryAddressService.createNewAddress(second, member.getId());

        // then
        List<DeliveryAddress> addresses =
            deliveryAddressRepository.findByMemberIdOrderByIsDefaultDescCreatedAtDesc(
                member.getId()
            );

        long defaultCount = addresses.stream()
            .filter(DeliveryAddress::getIsDefault)
            .count();

        assertThat(defaultCount).isEqualTo(1);

        DeliveryAddress defaultAddress = addresses.get(0);

        assertThat(defaultAddress.getLabel()).isEqualTo("회사");
        assertThat(defaultAddress.getIsDefault()).isTrue();
    }

    @Test
    @DisplayName("배송지는 최대 5개까지 등록 가능하다")
    void deliveryAddressLimitExceeded() {

        // given
        for (int i = 0; i < 5; i++) {

            DeliveryAddressRequest request = new DeliveryAddressRequest(
                "주소" + i,
                "홍길동",
                "01012345678",
                "12345",
                "서울시",
                null,
                "101호",
                null,
                false,
                null
            );

            deliveryAddressService.createNewAddress(
                request,
                member.getId()
            );
        }

        DeliveryAddressRequest overflowRequest =
            new DeliveryAddressRequest(
                "초과주소",
                "홍길동",
                "01012345678",
                "12345",
                "서울시",
                null,
                "101호",
                null,
                false,
                null
            );

        // when & then
        assertThatThrownBy(() ->
            deliveryAddressService.createNewAddress(
                overflowRequest,
                member.getId()
            )
        )
            .isInstanceOf(AppException.class)
            .hasMessage(
                MemberErrorCode.DELIVERY_ADDRESS_LIMIT_EXCEEDED.getMessage()
            );
    }

    @Test
    @DisplayName("배송지 삭제 성공")
    void deleteDeliveryAddress_success() {

        // given
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "집",
            "홍길동",
            "01012345678",
            "12345",
            "서울시",
            null,
            "101호",
            null,
            true,
            null
        );

        DeliveryAddressResponse response =
            deliveryAddressService.createNewAddress(
                request,
                member.getId()
            );

        // when
        deliveryAddressService.deleteAddress(
            response.id(),
            member.getId()
        );

        // then
        Optional<DeliveryAddress> deletedAddress =
            deliveryAddressRepository.findById(response.id());

        assertThat(deletedAddress).isPresent();
        assertThat(deletedAddress.get().isDeleted()).isTrue();
    }
}
