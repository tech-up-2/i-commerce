package com.example.i_commerce.domain.member.service.delivery;

import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.DeliveryAddressRepository;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressRequest;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressResponse;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressSnapshot;
import com.example.i_commerce.domain.member.service.delivery.dto.UpdateDeliveryAddressRequest;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;
    private final DataEncryptor dataEncryptor;

    //배송지 목록 조회
    @Transactional(readOnly = true)
    public List<DeliveryAddressResponse> getMyAddresses(Long memberId) {
        return deliveryAddressRepository
            .findByMemberIdOrderByIsDefaultDescCreatedAtDesc(memberId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private DeliveryAddressResponse toResponse(DeliveryAddress address) {
        return new DeliveryAddressResponse(
            address.getId(),
            address.getLabel(),
            dataEncryptor.decrypt(address.getRecipientName()),
            dataEncryptor.decrypt(address.getRecipientPhone()),
            dataEncryptor.decrypt(address.getZipCode()),
            dataEncryptor.decrypt(address.getRoadAddress()),
            decryptNullable(address.getJibunAddress()),
            dataEncryptor.decrypt(address.getDetailAddress()),
            decryptNullable(address.getExtraAddress()),
            address.getIsDefault(),
            decryptNullable(address.getDeliveryMemo())
        );
    }

    private byte[] encryptNullable(String value) {
        return value == null ? null : dataEncryptor.encrypt(value);
    }

    private String decryptNullable(byte[] value) {
        return value == null ? null : dataEncryptor.decrypt(value);
    }

    //새 배송지 등록
    @Transactional
    public DeliveryAddressResponse createNewAddress(
        DeliveryAddressRequest dto, Long memberId
    ) {
        long addressCount = deliveryAddressRepository.countByMemberId(memberId);

        if (addressCount >= 5) {//회원당 최대 배송지 갯수
            throw new AppException(MemberErrorCode.DELIVERY_ADDRESS_LIMIT_EXCEEDED);
        }

        //첫 배송지면 무조건 기본 배송지, 첫 배송지가 아니면 요청값이 true일 때만 기본 배송지
        boolean isDefault = addressCount == 0 || Boolean.TRUE.equals(dto.isDefault());

        if (isDefault) {
            clearDefaultAddresses(memberId);
        }
        //--------

        DeliveryAddress deliveryAddress = DeliveryAddress.builder()
            .memberId(memberId)
            .label(dto.label())
            .recipientName(dataEncryptor.encrypt(dto.recipientName()))
            .recipientPhone(dataEncryptor.encrypt(dto.recipientPhone()))
            .zipCode(dataEncryptor.encrypt(dto.zipCode()))
            .roadAddress(dataEncryptor.encrypt(dto.roadAddress()))
            .jibunAddress(encryptNullable(dto.jibunAddress()))
            .detailAddress(dataEncryptor.encrypt(dto.detailAddress()))
            .extraAddress(encryptNullable(dto.extraAddress()))
            .isDefault(isDefault)
            .deliveryMemo(encryptNullable(dto.deliveryMemo()))
            .build();

        DeliveryAddress savedAddress = deliveryAddressRepository.save(deliveryAddress);

        return toResponse(savedAddress);
    }

    //기존 배송지 수정
    @Transactional
    public DeliveryAddressResponse updateAddress(
        Long addressId,
        UpdateDeliveryAddressRequest dto,
        Long memberId
    ) {
        DeliveryAddress address = deliveryAddressRepository
            .findByIdAndMemberId(addressId, memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

        boolean isDefault = Boolean.TRUE.equals(dto.isDefault());

        if (isDefault && !address.getIsDefault()) {
            clearDefaultAddresses(memberId);
            address.changeDefault(true);
        }

        address.update(
            dto.label(),
            dataEncryptor.encrypt(dto.recipientName()),
            dataEncryptor.encrypt(dto.recipientPhone()),
            dataEncryptor.encrypt(dto.zipCode()),
            dataEncryptor.encrypt(dto.roadAddress()),
            encryptNullable(dto.jibunAddress()),
            dataEncryptor.encrypt(dto.detailAddress()),
            encryptNullable(dto.extraAddress()),
            encryptNullable(dto.deliveryMemo())
        );

        return toResponse(address);
    }

    //기본 배송지 변경
    @Transactional
    public void changeDefaultAddress(
        Long addressId,
        Long memberId
    ) {
        DeliveryAddress address = deliveryAddressRepository
            .findByIdAndMemberId(addressId, memberId)
            .orElseThrow(() ->
                new AppException(MemberErrorCode.DELIVERY_ADDRESS_NOT_FOUND)
            );

        clearDefaultAddresses(memberId);
        address.changeDefault(true);
    }

    //배송지 삭제
    @Transactional
    public void deleteAddress(Long addressId, Long memberId) {
        DeliveryAddress address = deliveryAddressRepository
            .findByIdAndMemberId(addressId, memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

        address.delete();
    }

    //주문시 사용
    @Transactional(readOnly = true)
    public DeliveryAddressSnapshot getAddressSnapshot(
        Long addressId,
        Long memberId
    ) {
        DeliveryAddress address = deliveryAddressRepository
            .findByIdAndMemberId(addressId, memberId)
            .orElseThrow(() -> new AppException(MemberErrorCode.DELIVERY_ADDRESS_NOT_FOUND));

        return new DeliveryAddressSnapshot(
            dataEncryptor.decrypt(address.getRecipientName()),
            dataEncryptor.decrypt(address.getRecipientPhone()),
            dataEncryptor.decrypt(address.getZipCode()),
            dataEncryptor.decrypt(address.getRoadAddress()),
            decryptNullable(address.getJibunAddress()),
            dataEncryptor.decrypt(address.getDetailAddress()),
            decryptNullable(address.getExtraAddress()),
            decryptNullable(address.getDeliveryMemo())
        );
    }

    private void clearDefaultAddresses(Long memberId) {

        List<DeliveryAddress> addresses =
            deliveryAddressRepository
                .findByMemberIdOrderByIsDefaultDescCreatedAtDesc(memberId);

        for (DeliveryAddress address : addresses) {
            if (address.getIsDefault()) {
                address.changeDefault(false);
            }
        }
    }
}