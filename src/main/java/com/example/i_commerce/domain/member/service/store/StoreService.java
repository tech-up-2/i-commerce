package com.example.i_commerce.domain.member.service.store;

import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.StoreAddress;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.repository.StoreAddressRepository;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.member.service.store.dto.StoreAddressRequest;
import com.example.i_commerce.domain.member.service.store.dto.StoreAddressResponse;
import com.example.i_commerce.domain.member.service.store.dto.StoreInfoResponse;
import com.example.i_commerce.domain.member.service.store.dto.StoreRequest;
import com.example.i_commerce.domain.member.service.store.dto.StoreResponse;
import com.example.i_commerce.domain.member.service.store.dto.StoreUpdateRequest;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final SellerRepository sellerRepository;
    private final StoreAddressRepository storeAddressRepository;

    //상점 개설
    @Transactional
    public StoreResponse createStore(Long sellerId, StoreRequest dto) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND));

        Store store = Store.builder()
            .sellerId(seller.getId())
            .storeName(dto.storeName())
            .phoneNumber(dto.phoneNumber())
            .build();

        Store savedStore = storeRepository.save(store);

        return new StoreResponse(
            savedStore.getId(),
            savedStore.getStoreName(),
            savedStore.getStoreStatus());
    }

    //내 상점 목록 조회
    @Transactional(readOnly = true)
    public List<StoreResponse> getMyStores(Long sellerId) {

        return storeRepository.findAllBySellerIdAndDeletedAtIsNull(sellerId)
            .stream()
            .map(StoreResponse::from)
            .toList();
    }

    //상점 상세 조회
    @Transactional(readOnly = true)
    public StoreInfoResponse getMyStoreInfo(Long storeId, Long sellerId) {
        Store store = storeRepository
            .findByIdAndSellerIdAndDeletedAtIsNull(storeId, sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_NOT_FOUND));

        return StoreInfoResponse.from(store);
    }

    //상점 정보 수정
    @Transactional
    public StoreResponse updateStoreInfo(Long storeId, StoreUpdateRequest dto, Long sellerId) {
        Store store = storeRepository
            .findByIdAndSellerIdAndDeletedAtIsNull(storeId, sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_NOT_FOUND));

        store.update(dto);

        return StoreResponse.from(store);
    }

    //상점 삭제
    @Transactional
    public void deleteStore(Long storeId, Long sellerId) {
        Store store = storeRepository
            .findByIdAndSellerIdAndDeletedAtIsNull(storeId, sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_NOT_FOUND));

        store.delete();
    }

    //상점 주소 목록 조회
    @Transactional(readOnly = true)
    public List<StoreAddressResponse> getMyStoreAddresses(Long storeId, Long sellerId) {

        Store store = storeRepository.findByIdAndSellerIdAndDeletedAtIsNull(storeId, sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_NOT_FOUND));

        return storeAddressRepository.findByStoreIdOrderByIsDefaultDescCreatedAtDesc(store.getId())
            .stream()
            .map(StoreAddressResponse::from)
            .toList();
    }

    //상점 주소 등록
    @Transactional
    public StoreAddressResponse createStoreAddress(Long storeId, Long sellerId,
        StoreAddressRequest dto) {
        Store store = storeRepository.findByIdAndSellerIdAndDeletedAtIsNull(storeId, sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_NOT_FOUND));

        long storeAddressCount = storeAddressRepository.countByStoreIdAndDeletedAtIsNull(
            store.getId());

        //첫 배송지면 무조건 기본 배송지, 첫 배송지가 아니면 요청값이 true일 때만 기본 배송지
        boolean isDefault = storeAddressCount == 0 || Boolean.TRUE.equals(dto.isDefault());

        if (isDefault) {
            clearDefaultAddresses(store.getId());
        }

        StoreAddress storeAddress = StoreAddress.builder()
            .storeId(store.getId())
            .addressType(dto.addressType())
            .label(dto.label())
            .addressPhoneNumber(dto.addressPhoneNumber())
            .zipCode(dto.zipCode())
            .roadAddress(dto.roadAddress())
            .jibunAddress(dto.jibunAddress())
            .detailAddress(dto.detailAddress())
            .extraAddress(dto.extraAddress())
            .isDefault(isDefault)
            .build();

        StoreAddress savedStoreAddress = storeAddressRepository.save(storeAddress);

        return StoreAddressResponse.from(savedStoreAddress);
    }

    //상점 주소 수정
    @Transactional
    public StoreAddressResponse updateStoreAddress(
        Long addressId,
        Long storeId,
        Long sellerId,
        StoreAddressRequest dto
    ) {
        Store store = storeRepository.findByIdAndSellerIdAndDeletedAtIsNull(storeId, sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_NOT_FOUND));

        StoreAddress address = storeAddressRepository
            .findStoreAddressByIdAndStoreIdAndDeletedAtIsNull(addressId, store.getId())
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_ADDRESS_NOT_FOUND));

        boolean isDefault = Boolean.TRUE.equals(dto.isDefault());

        if (isDefault && !address.getIsDefault()) {
            clearDefaultAddresses(store.getId());
            address.changeDefault(true);
        }

        address.update(dto);

        return StoreAddressResponse.from(address);
    }

    //상점 주소 삭제
    @Transactional
    public void deleteStoreAddress(Long addressId, Long storeId, Long sellerId) {
        Store store = storeRepository.findByIdAndSellerIdAndDeletedAtIsNull(storeId, sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_NOT_FOUND));

        StoreAddress address = storeAddressRepository
            .findStoreAddressByIdAndStoreIdAndDeletedAtIsNull(addressId, store.getId())
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_ADDRESS_NOT_FOUND));

        boolean wasDefault = address.getIsDefault();

        address.delete();

        if (wasDefault) {
            storeAddressRepository.findFirstByStoreIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                    store.getId())
                .ifPresent(next -> next.changeDefault(true));
        }
    }

    //기본주소 설정
    @Transactional
    public void changeDefault(Long addressId, Long storeId, Long sellerId) {
        Store store = storeRepository.findByIdAndSellerIdAndDeletedAtIsNull(storeId, sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_NOT_FOUND));

        StoreAddress address = storeAddressRepository
            .findStoreAddressByIdAndStoreIdAndDeletedAtIsNull(addressId, store.getId())
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_ADDRESS_NOT_FOUND));

        clearDefaultAddresses(store.getId());
        address.changeDefault(true);
    }


    //상점이 가지고 있는 주소의 기본여부를 전부 false로 변경
    private void clearDefaultAddresses(Long storeId) {

        List<StoreAddress> addresses =
            storeAddressRepository
                .findByStoreIdOrderByIsDefaultDescCreatedAtDesc(storeId);

        for (StoreAddress address : addresses) {
            if (address.getIsDefault()) {
                address.changeDefault(false);
            }
        }
    }

    //현재 로그인된 유저가 store 관리자인지 검증
    @Transactional(readOnly = true)
    public boolean isStoreManager(Long userId, Long storeId) {
        Store store = storeRepository.findByIdAndDeletedAtIsNull(storeId)
            .orElseThrow(() -> new AppException(MemberErrorCode.STORE_NOT_FOUND));

        return store.getSellerId().equals(userId);
    }
}
