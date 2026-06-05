package com.example.i_commerce.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.StoreAddress;
import com.example.i_commerce.domain.member.entity.enums.AddressType;
import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import com.example.i_commerce.domain.member.repository.StoreAddressRepository;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.member.service.store.dto.StoreAddressRequest;
import com.example.i_commerce.domain.member.service.store.dto.StoreAddressResponse;
import com.example.i_commerce.domain.member.service.store.dto.StoreInfoResponse;
import com.example.i_commerce.domain.member.service.store.dto.StoreRequest;
import com.example.i_commerce.domain.member.service.store.dto.StoreResponse;
import com.example.i_commerce.domain.member.service.store.dto.StoreUpdateRequest;
import com.example.i_commerce.domain.testtools.IntegrationTestSupport;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "file:.env")
class StoreServiceTest extends IntegrationTestSupport {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreAddressRepository storeAddressRepository;

    private Long sellerId;

    @BeforeEach
    void setUpCommon() {
        CustomUserPrincipal seller = loginAsApprovedSeller();
        sellerId = seller.getId();
    }

    private StoreAddressRequest createMockAddressRequest(String label, boolean isDefault) {
        return new StoreAddressRequest(
            AddressType.BUSINESS,
            label,
            "010-1111-1111",
            "12345",
            "부산광역시 동구 중앙대로 1",
            "부산광역시 동구 초량동 1",
            "101호",
            "",
            isDefault
        );
    }

    @Nested
    @DisplayName("상점 생성 및 목록 조회 테스트")
    class StoreLifecycle {

        @Test
        @DisplayName("새로운 상점을 성공적으로 생성한다")
        void createStore_success() {
            // given
            StoreRequest storeRequest = new StoreRequest("테스트 상점", "010-3333-4444");

            // when
            StoreResponse response = storeService.createStore(sellerId, storeRequest);

            // then
            List<Store> stores = storeRepository.findAllBySellerIdAndDeletedAtIsNull(sellerId);
            assertThat(stores).hasSize(1);

            Store savedStore = stores.get(0);
            assertThat(savedStore.getStoreName()).isEqualTo("테스트 상점");
            assertThat(savedStore.getPhoneNumber()).isEqualTo("010-3333-4444");
            assertThat(response.storeName()).isEqualTo("테스트 상점");
        }

        @Test
        @DisplayName("내 상점 목록을 조회한다")
        void getMyStores_success() {
            // given
            storeService.createStore(sellerId, new StoreRequest("상점1", "010-1111-1111"));
            storeService.createStore(sellerId, new StoreRequest("상점2", "010-2222-2222"));

            // when
            List<StoreResponse> responses = storeService.getMyStores(sellerId);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses)
                .extracting("storeName")
                .containsExactlyInAnyOrder("상점1", "상점2");
        }
    }

    @Nested
    @DisplayName("상점 정보 관리 테스트")
    class StoreManagement {

        private Long storeId;

        @BeforeEach
        void setUpStore() {
            Store store = openStore(sellerId);
            storeId = store.getId();
        }

        @Test
        @DisplayName("상점 상세 정보를 조회한다")
        void getMyStoreInfo_success() {
            // when
            StoreInfoResponse response = storeService.getMyStoreInfo(storeId, sellerId);

            // then
            assertThat(response.storeName()).isNotBlank();
            assertThat(response.phoneNumber()).isNotBlank();
        }

        @Test
        @DisplayName("상점 정보를 성공적으로 수정한다")
        void updateStoreInfo_success() {
            // given
            StoreUpdateRequest updateRequest = new StoreUpdateRequest(
                "수정된 상점",
                "010-8888-8888",
                StoreStatus.OPEN
            );

            // when
            StoreResponse response = storeService.updateStoreInfo(storeId, updateRequest, sellerId);

            // then
            Store updatedStore = storeRepository.findById(storeId).orElseThrow();

            assertThat(response.storeName()).isEqualTo("수정된 상점");
            assertThat(updatedStore.getStoreName()).isEqualTo("수정된 상점");
            assertThat(updatedStore.getPhoneNumber()).isEqualTo("010-8888-8888");
        }

        @Test
        @DisplayName("상점을 삭제(소프트 딜리트)한다")
        void deleteStore_success() {
            // when
            storeService.deleteStore(storeId, sellerId);

            // then
            Store deletedStore = storeRepository.findById(storeId).orElseThrow();
            assertThat(deletedStore.getDeletedAt()).isNotNull();

            List<Store> activeStores =
                storeRepository.findAllBySellerIdAndDeletedAtIsNull(sellerId);

            assertThat(activeStores).isEmpty();
        }
    }

    @Nested
    @DisplayName("상점 주소 등록 테스트")
    class StoreAddressCreation {

        private Long storeId;

        @BeforeEach
        void setUpStore() {
            Store store = openStore(sellerId);
            storeId = store.getId();
        }

        @Test
        @DisplayName("첫 주소를 등록하면 자동으로 기본 주소가 된다")
        void createFirstAddress_becomesDefault() {
            // given
            StoreAddressRequest request = createMockAddressRequest("첫 주소", false);

            // when
            StoreAddressResponse response =
                storeService.createStoreAddress(storeId, sellerId, request);

            // then
            StoreAddress savedAddress =
                storeAddressRepository.findById(response.storeAddressId().longValue())
                    .orElseThrow();

            assertThat(savedAddress.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("새로운 기본 주소를 등록하면 기존 기본 주소는 해제된다")
        void createDefaultAddress_overridesOldDefault() {
            // given
            storeService.createStoreAddress(
                storeId,
                sellerId,
                createMockAddressRequest("기존 주소", true)
            );

            StoreAddressRequest newDefaultRequest =
                createMockAddressRequest("새 기본 주소", true);

            // when
            storeService.createStoreAddress(storeId, sellerId, newDefaultRequest);

            // then
            List<StoreAddress> addresses =
                storeAddressRepository.findByStoreIdOrderByIsDefaultDescCreatedAtDesc(storeId);

            assertThat(addresses).hasSize(2);
            assertThat(addresses.get(0).getLabel()).isEqualTo("새 기본 주소");
            assertThat(addresses.get(0).getIsDefault()).isTrue();
            assertThat(addresses.get(1).getLabel()).isEqualTo("기존 주소");
            assertThat(addresses.get(1).getIsDefault()).isFalse();
        }
    }

    @Nested
    @DisplayName("상점 주소 관리 및 삭제 로직 테스트")
    class StoreAddressManagement {

        private Long storeId;
        private StoreAddress defaultAddress;
        private StoreAddress oldAddress;
        private StoreAddress latestAddress;

        @BeforeEach
        void setUpAddresses() {
            Store store = openStore(sellerId);
            storeId = store.getId();

            defaultAddress = createDefaultStoreAddress(storeId, "기본 주소");
            oldAddress = createNormalStoreAddress(storeId, "이전 일반 주소");
            latestAddress = createNormalStoreAddress(storeId, "최신 일반 주소");
        }

        @Test
        @DisplayName("주소를 수정하며 기본 주소로 설정하면 기존 기본 주소는 해제된다")
        void updateAddress_toDefault() {
            // given
            StoreAddressRequest updateRequest = createMockAddressRequest("수정된 주소", true);

            // when
            storeService.updateStoreAddress(
                latestAddress.getId(),
                storeId,
                sellerId,
                updateRequest
            );

            // then
            StoreAddress updated =
                storeAddressRepository.findById(latestAddress.getId()).orElseThrow();

            StoreAddress oldDefault =
                storeAddressRepository.findById(defaultAddress.getId()).orElseThrow();

            assertThat(updated.getLabel()).isEqualTo("수정된 주소");
            assertThat(updated.getIsDefault()).isTrue();
            assertThat(oldDefault.getIsDefault()).isFalse();
        }

        @Test
        @DisplayName("특정 주소를 기본 주소로 명시적 변경한다")
        void changeDefault_success() {
            // when
            storeService.changeDefault(oldAddress.getId(), storeId, sellerId);

            // then
            StoreAddress changedDefault =
                storeAddressRepository.findById(oldAddress.getId()).orElseThrow();

            StoreAddress previousDefault =
                storeAddressRepository.findById(defaultAddress.getId()).orElseThrow();

            assertThat(changedDefault.getIsDefault()).isTrue();
            assertThat(previousDefault.getIsDefault()).isFalse();
        }

        @Test
        @DisplayName("기본주소를 삭제하면 남아있는 최신 주소가 기본주소가 된다")
        void deleteDefaultAddress_thenLatestRemainingAddressBecomesDefault() {
            // when
            storeService.deleteStoreAddress(defaultAddress.getId(), storeId, sellerId);

            // then
            StoreAddress deletedAddress =
                storeAddressRepository.findById(defaultAddress.getId()).orElseThrow();

            StoreAddress reloadedOldAddress =
                storeAddressRepository.findById(oldAddress.getId()).orElseThrow();

            StoreAddress reloadedLatestAddress =
                storeAddressRepository.findById(latestAddress.getId()).orElseThrow();

            assertThat(deletedAddress.getDeletedAt()).isNotNull();
            assertThat(reloadedOldAddress.getIsDefault()).isFalse();
            assertThat(reloadedLatestAddress.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("기본주소가 아닌 주소를 삭제하면 기존 기본주소는 그대로 유지된다")
        void deleteNonDefaultAddress_thenDefaultAddressDoesNotChange() {
            // when
            storeService.deleteStoreAddress(oldAddress.getId(), storeId, sellerId);

            // then
            StoreAddress reloadedDefaultAddress =
                storeAddressRepository.findById(defaultAddress.getId()).orElseThrow();

            StoreAddress deletedAddress =
                storeAddressRepository.findById(oldAddress.getId()).orElseThrow();

            assertThat(reloadedDefaultAddress.getIsDefault()).isTrue();
            assertThat(reloadedDefaultAddress.getDeletedAt()).isNull();
            assertThat(deletedAddress.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("마지막 남은 기본주소를 삭제하면 기본주소는 존재하지 않는다")
        void deleteLastDefaultAddress_thenNoDefaultAddressExists() {
            // given
            storeService.deleteStoreAddress(oldAddress.getId(), storeId, sellerId);
            storeService.deleteStoreAddress(latestAddress.getId(), storeId, sellerId);

            // when
            storeService.deleteStoreAddress(defaultAddress.getId(), storeId, sellerId);

            // then
            StoreAddress deletedAddress =
                storeAddressRepository.findById(defaultAddress.getId()).orElseThrow();

            Optional<StoreAddress> anyDefaultAddress =
                storeAddressRepository.findByStoreIdAndIsDefaultTrueAndDeletedAtIsNull(storeId);

            assertThat(deletedAddress.getDeletedAt()).isNotNull();
            assertThat(anyDefaultAddress).isEmpty();
        }
    }
}