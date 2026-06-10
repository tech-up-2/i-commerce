package com.example.i_commerce.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.DeliveryAddressRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.service.delivery.DeliveryAddressService;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressRequest;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressResponse;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressSnapshot;
import com.example.i_commerce.domain.member.service.delivery.dto.UpdateDeliveryAddressRequest;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.testtools.IntegrationTestSupport;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
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
class DeliveryAddressServiceTest extends IntegrationTestSupport {

    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DataEncryptor dataEncryptor;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CustomUserPrincipal member;

    @BeforeEach
    void setUp() {
        member = loginAsActiveMaleMember();
    }

    /*
    배송지 등록 테스트
     */
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
    @DisplayName("새로 추가되는 배송지가 기본배송지 일때 기존 기본 배송지는 해제된다")
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
            DeliveryAddress address = createNormalDeliveryAddress(member.getId());
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

    /*
    배송지 수정 테스트
     */
    @Test
    @DisplayName("배송지 수정 성공")
    void updateAddress_success() throws Exception {
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "배송지",
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

        DeliveryAddressResponse deliveryAddress = deliveryAddressService.createNewAddress(request,
            member.getId());

        UpdateDeliveryAddressRequest updateRequest = new UpdateDeliveryAddressRequest(
            "수정된 배송지",
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

        deliveryAddressService.updateAddress(deliveryAddress.id(), updateRequest, member.getId());

        Optional<DeliveryAddress> address =
            deliveryAddressRepository.findByIdAndMemberId(deliveryAddress.id(), member.getId());

        assertThat(address.get().getLabel()).isEqualTo("수정된 배송지");
    }

    @Test
    @DisplayName("배송지 수정 성공 - 기본 배송지")
    void updatdAddress_success_isdefault() throws Exception {
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "배송지",
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

        DeliveryAddressResponse deliveryAddress = deliveryAddressService.createNewAddress(request,
            member.getId());

        UpdateDeliveryAddressRequest updateRequest = new UpdateDeliveryAddressRequest(
            "수정된 배송지",
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

        deliveryAddressService.updateAddress(deliveryAddress.id(), updateRequest, member.getId());

        Optional<DeliveryAddress> address =
            deliveryAddressRepository.findByIdAndMemberId(deliveryAddress.id(), member.getId());

        assertThat(address.get().getIsDefault()).isEqualTo(true);
    }

    @Test
    @DisplayName("배송지 수정 성공 - 기본 배송지2")
    void updatdAddress_success_isdefault2() throws Exception {
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "배송지",
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

        DeliveryAddressRequest request2 = new DeliveryAddressRequest(
            "배송지2",
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

        DeliveryAddressResponse deliveryAddress = deliveryAddressService.createNewAddress(request,
            member.getId());
        deliveryAddressService.createNewAddress(request2, member.getId());

        UpdateDeliveryAddressRequest updateRequest = new UpdateDeliveryAddressRequest(
            "수정된 배송지",
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

        deliveryAddressService.updateAddress(deliveryAddress.id(), updateRequest, member.getId());

        Optional<DeliveryAddress> address =
            deliveryAddressRepository.findByIdAndMemberId(deliveryAddress.id(), member.getId());

        assertThat(address.get().getIsDefault()).isEqualTo(true);
    }

    @Test
    @DisplayName("배송지 수정 실패 - 배송지를 찾을 수 없음")
    void updatdAddress_fail_addressnotfound() {
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "배송지",
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

        DeliveryAddressResponse deliveryAddress =
            deliveryAddressService.createNewAddress(request, member.getId());

        UpdateDeliveryAddressRequest updateRequest = new UpdateDeliveryAddressRequest(
            "수정된 배송지",
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

        AppException exception = assertThrows(
            AppException.class,
            () -> deliveryAddressService.updateAddress(50L, updateRequest, member.getId())
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
    }

    /*
    기본 배송지 변경
     */
    @Test
    @DisplayName("기본 배송지 변경 성공")
    void changeDefaultAddress_success() throws Exception {
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "배송지",
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

        DeliveryAddressRequest request2 = new DeliveryAddressRequest(
            "배송지2",
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

        DeliveryAddressResponse deliveryAddress = deliveryAddressService.createNewAddress(request,
            member.getId());
        deliveryAddressService.createNewAddress(request2, member.getId());

        deliveryAddressService.changeDefaultAddress(deliveryAddress.id(), member.getId());

        Optional<DeliveryAddress> address = deliveryAddressRepository.findById(
            deliveryAddress.id());

        assertThat(address.get().getIsDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 변경 실패")
    void changeDefaultAddress_fail() {
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "배송지",
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

        DeliveryAddressRequest request2 = new DeliveryAddressRequest(
            "배송지2",
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

        deliveryAddressService.createNewAddress(request,
            member.getId());
        deliveryAddressService.createNewAddress(request2, member.getId());

        AppException exception = assertThrows(
            AppException.class,
            () -> deliveryAddressService.changeDefaultAddress(null, member.getId()));

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
    }

    /*
    배송지 조회
     */
    @Test
    @DisplayName("배송지 목록 조회 - 비어있음")
    void getMyAddresses_success() throws Exception {
        List<DeliveryAddressResponse> mydelivery = deliveryAddressService.getMyAddresses(
            member.getId());
        assertThat(mydelivery).isEmpty();
    }

    @Test
    @DisplayName("배송지 목록 조회 - 주소 하나")
    void getMyAddresses_success_onedelivery() throws Exception {
        // given
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "배송지",
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

        DeliveryAddressResponse deliveryAddress = deliveryAddressService.createNewAddress(
            request,
            member.getId()
        );

        // when
        List<DeliveryAddressResponse> myDelivery = deliveryAddressService.getMyAddresses(
            member.getId()
        );

        // then
        assertAll(
            () -> assertThat(myDelivery).hasSize(1),
            () -> assertThat(myDelivery.get(0).id()).isEqualTo(deliveryAddress.id()),
            () -> assertThat(myDelivery.get(0).label()).isEqualTo("배송지"),
            () -> assertThat(myDelivery.get(0).recipientName()).isEqualTo("홍길동"),
            () -> assertThat(myDelivery.get(0).recipientPhone()).isEqualTo("01012345678"),
            () -> assertThat(myDelivery.get(0).zipCode()).isEqualTo("12345"),
            () -> assertThat(myDelivery.get(0).roadAddress()).isEqualTo("서울시"),
            () -> assertThat(myDelivery.get(0).detailAddress()).isEqualTo("101호")
        );
    }

    @Test
    @DisplayName("배송지 목록 조회 - 기본 배송지가 가장 먼저 나오고 나머지는 최신 등록순으로 조회된다")
    void getMyAddresses_success_defaultFirstAndCreatedAtDesc() {
        // given
        DeliveryAddressResponse address1 = deliveryAddressService.createNewAddress(
            createDeliveryAddressRequest("배송지1", "홍길동1", "01011111111", false),
            member.getId()
        );

        DeliveryAddressResponse address2 = deliveryAddressService.createNewAddress(
            createDeliveryAddressRequest("배송지2", "홍길동2", "01022222222", false),
            member.getId()
        );

        DeliveryAddressResponse address3 = deliveryAddressService.createNewAddress(
            createDeliveryAddressRequest("배송지3", "홍길동3", "01033333333", false),
            member.getId()
        );

        DeliveryAddressResponse address4 = deliveryAddressService.createNewAddress(
            createDeliveryAddressRequest("배송지4", "홍길동4", "01044444444", false),
            member.getId()
        );

        DeliveryAddressResponse address5 = deliveryAddressService.createNewAddress(
            createDeliveryAddressRequest("배송지5", "홍길동5", "01055555555", true),
            member.getId()
        );

        // when
        List<DeliveryAddressResponse> myAddresses = deliveryAddressService.getMyAddresses(
            member.getId()
        );

        // then
        assertAll(
            () -> assertThat(myAddresses).hasSize(5),

            // 기본 배송지가 가장 먼저 조회되어야 함
            () -> assertThat(myAddresses.get(0).id()).isEqualTo(address5.id()),
            () -> assertThat(myAddresses.get(0).isDefault()).isTrue(),

            // 나머지는 createdAt desc, 즉 최신 등록순
            () -> assertThat(myAddresses.get(1).id()).isEqualTo(address4.id()),
            () -> assertThat(myAddresses.get(2).id()).isEqualTo(address3.id()),
            () -> assertThat(myAddresses.get(3).id()).isEqualTo(address2.id()),
            () -> assertThat(myAddresses.get(4).id()).isEqualTo(address1.id()),

            () -> assertThat(myAddresses.get(1).isDefault()).isFalse(),
            () -> assertThat(myAddresses.get(2).isDefault()).isFalse(),
            () -> assertThat(myAddresses.get(3).isDefault()).isFalse(),
            () -> assertThat(myAddresses.get(4).isDefault()).isFalse()
        );
    }

    private DeliveryAddressRequest createDeliveryAddressRequest(
        String label,
        String recipientName,
        String recipientPhone,
        boolean isDefault
    ) {
        return new DeliveryAddressRequest(
            label,
            recipientName,
            recipientPhone,
            "12345",
            "서울시",
            null,
            "101호",
            null,
            isDefault,
            null
        );
    }

    @Test
    @DisplayName("배송지 삭제 성공")
    void deleteDeliveryAddress_success() {

        // given
        DeliveryAddress address = createDefaultDeliveryAddress(member.getId());

        // when
        deliveryAddressService.deleteAddress(
            address.getId(),
            member.getId()
        );

        // then
        Optional<DeliveryAddress> deletedAddress =
            deliveryAddressRepository.findById(address.getId());

        assertThat(deletedAddress).isPresent();
        assertThat(deletedAddress.get().isDeleted()).isTrue();
    }

    /*
    주문시 사용하는 메서드 테스트
     */
    @Test
    @DisplayName("배송지 스냅샷 조회 성공")
    void getAddressSnapshot_success() {
        // given
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "집",
            "홍길동",
            "01012345678",
            "12345",
            "서울시 강남구",
            "서울시 지번주소",
            "101호",
            "추가주소",
            false,
            "문 앞에 놓아주세요"
        );

        DeliveryAddressResponse savedAddress = deliveryAddressService.createNewAddress(
            request,
            member.getId()
        );

        // when
        DeliveryAddressSnapshot snapshot = deliveryAddressService.getAddressSnapshot(
            savedAddress.id(),
            member.getId()
        );

        // then
        assertAll(
            () -> assertThat(snapshot.recipientName()).isEqualTo("홍길동"),
            () -> assertThat(snapshot.recipientPhone()).isEqualTo("01012345678"),
            () -> assertThat(snapshot.zipCode()).isEqualTo("12345"),
            () -> assertThat(snapshot.roadAddress()).isEqualTo("서울시 강남구"),
            () -> assertThat(snapshot.jibunAddress()).isEqualTo("서울시 지번주소"),
            () -> assertThat(snapshot.detailAddress()).isEqualTo("101호"),
            () -> assertThat(snapshot.extraAddress()).isEqualTo("추가주소"),
            () -> assertThat(snapshot.deliveryMemo()).isEqualTo("문 앞에 놓아주세요")
        );
    }

    @Test
    @DisplayName("배송지 스냅샷 조회 성공 - 선택값이 null이면 null로 반환된다")
    void getAddressSnapshot_success_nullableFields() {
        // given
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "집",
            "홍길동",
            "01012345678",
            "12345",
            "서울시 강남구",
            null,
            "101호",
            null,
            false,
            null
        );

        DeliveryAddressResponse savedAddress = deliveryAddressService.createNewAddress(
            request,
            member.getId()
        );

        // when
        DeliveryAddressSnapshot snapshot = deliveryAddressService.getAddressSnapshot(
            savedAddress.id(),
            member.getId()
        );

        // then
        assertAll(
            () -> assertThat(snapshot.recipientName()).isEqualTo("홍길동"),
            () -> assertThat(snapshot.recipientPhone()).isEqualTo("01012345678"),
            () -> assertThat(snapshot.zipCode()).isEqualTo("12345"),
            () -> assertThat(snapshot.roadAddress()).isEqualTo("서울시 강남구"),
            () -> assertThat(snapshot.jibunAddress()).isNull(),
            () -> assertThat(snapshot.detailAddress()).isEqualTo("101호"),
            () -> assertThat(snapshot.extraAddress()).isNull(),
            () -> assertThat(snapshot.deliveryMemo()).isNull()
        );
    }

    @Test
    @DisplayName("배송지 스냅샷 조회 실패 - 존재하지 않는 배송지")
    void getAddressSnapshot_fail_addressNotFound() {
        // given
        Long notFoundAddressId = 999999L;

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> deliveryAddressService.getAddressSnapshot(
                notFoundAddressId,
                member.getId()
            )
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
    }

    @Test
    @DisplayName("배송지 스냅샷 조회 실패 - 다른 회원의 배송지")
    void getAddressSnapshot_fail_otherMemberAddress() {
        // given
        DeliveryAddressRequest request = new DeliveryAddressRequest(
            "집",
            "홍길동",
            "01012345678",
            "12345",
            "서울시 강남구",
            null,
            "101호",
            null,
            false,
            null
        );

        DeliveryAddressResponse savedAddress = deliveryAddressService.createNewAddress(
            request,
            member.getId()
        );

        Member otherMember = memberRepository.save(Member.builder()
            .emailHash("other-email-hash")
            .emailEncrypted(dataEncryptor.encrypt("other@test.com"))
            .password(passwordEncoder.encode("password123!"))
            .name(dataEncryptor.encrypt("다른회원"))
            .sex(Gender.MALE)
            .birthday(dataEncryptor.encrypt("2000-01-01"))
            .phoneNumber(dataEncryptor.encrypt("01099999999"))
            .build()
        );

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> deliveryAddressService.getAddressSnapshot(
                savedAddress.id(),
                otherMember.getId()
            )
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
    }
}
