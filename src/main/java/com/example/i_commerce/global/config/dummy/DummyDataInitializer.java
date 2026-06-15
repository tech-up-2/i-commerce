package com.example.i_commerce.global.config.dummy;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.StoreAddress;
import com.example.i_commerce.domain.member.entity.enums.AddressType;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.repository.DeliveryAddressRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.repository.StoreAddressRepository;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Profile("dummy")
@Component
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {

    private static final String PASSWORD = "password123!";
    // 필요하면 이 숫자만 조절하면 됨
    private static final int MEMBER_COUNT_PER_STATUS = 250;
    private static final int SELLER_COUNT_PER_STATUS = 25;
    private static final int ADMIN_COUNT_PER_ROLE = 3;
    private static final int DELIVERY_ADDRESS_COUNT_PER_MEMBER = 2;
    private static final int STORE_COUNT_PER_SELLER = 2;
    private static final int STORE_ADDRESS_COUNT_PER_STORE = 3;
    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;
    private final StoreRepository storeRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final StoreAddressRepository storeAddressRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailHashEncoder emailHashEncoder;
    private final DataEncryptor dataEncryptor;
    private String encodedPassword;
    private byte[] encryptedBirthday;
    private byte[] encryptedPhoneNumber;
    private byte[] encryptedZipCode;
    private byte[] encryptedRecipientPhone;
    private byte[] encryptedBankName;
    private byte[] encryptedBankAccount;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("더미 데이터 생성을 시작합니다.");

        this.encodedPassword = passwordEncoder.encode(PASSWORD);
        this.encryptedBirthday = dataEncryptor.
            encrypt(LocalDate.of(1999, 1, 1).toString());
        this.encryptedPhoneNumber = dataEncryptor.encrypt("01012345678");
        this.encryptedZipCode = dataEncryptor.encrypt("12345");
        this.encryptedRecipientPhone = dataEncryptor.encrypt("01012345678");
        this.encryptedBankName = dataEncryptor.encrypt("국민은행");
        this.encryptedBankAccount = dataEncryptor.encrypt("12345678901234");

        createMembers();
        createSellers();
        createAdmins();

        log.info("더미 데이터 생성이 완료되었습니다.");
    }

    /**
     * 일반 회원 생성
     * <p>
     * email 규칙: activeMember1@test.com inactiveMember1@test.com suspendedMember1@test.com
     * withdrawnMember1@test.com
     */
    private void createMembers() {
        for (MemberStatus status : MemberStatus.values()) {
            for (int i = 1; i <= MEMBER_COUNT_PER_STATUS; i++) {
                String email = status.name().toLowerCase() + "Member" + i + "@test.com";
                String name = "일반회원" + i;

                Member member = createMemberIfNotExists(
                    email,
                    name,
                    MemberType.CUSTOMER,
                    status,
                    false,
                    i
                );

                createDeliveryAddressesIfNotExists(member, name);
            }
        }
    }

    /**
     * 판매자 생성
     * <p>
     * email 규칙: pendingSeller1@test.com approvedSeller1@test.com blockedSeller1@test.com
     * withdrawSeller1@test.com
     * <p>
     * 주의: 판매자는 SellerStatus 테스트가 목적이므로 MemberStatus는 ACTIVE로 고정한다.
     */
    private void createSellers() {
        for (SellerStatus sellerStatus : SellerStatus.values()) {
            for (int i = 1; i <= SELLER_COUNT_PER_STATUS; i++) {
                String email = sellerStatus.name().toLowerCase() + "Seller" + i + "@test.com";
                String name = "판매자" + i;

                Member sellerMember = createMemberIfNotExists(
                    email,
                    name,
                    MemberType.SELLER,
                    MemberStatus.ACTIVE,
                    true,
                    i
                );

                Seller seller = createSellerIfNotExists(
                    sellerMember,
                    name,
                    sellerStatus,
                    i
                );

                createStoresIfNotExists(seller, name);
            }
        }
    }

    /**
     * 관리자 생성
     * <p>
     * email 규칙: activeMaster1@test.com activeAdmin1@test.com activeOperator1@test.com
     * lockedMaster1@test.com withdrawnAdmin1@test.com
     */
    private void createAdmins() {
        for (AdminStatus status : AdminStatus.values()) {
            for (AdminRole role : AdminRole.values()) {
                for (int i = 1; i <= ADMIN_COUNT_PER_ROLE; i++) {
                    String email = status.name().toLowerCase()
                        + capitalize(role.name().toLowerCase())
                        + i
                        + "@test.com";

                    String name = "관리자" + i;

                    createAdminIfNotExists(email, name, role, status);
                }
            }
        }
    }

    private Member createMemberIfNotExists(
        String email,
        String name,
        MemberType memberType,
        MemberStatus memberStatus,
        Boolean isSeller,
        int number
    ) {
        String emailHash = emailHashEncoder.encode(email);

        Gender gender = number % 2 == 0
            ? Gender.FEMALE : Gender.MALE;

        return memberRepository.findByEmailHash(emailHash)
            .orElseGet(() -> {
                Member member = Member.builder()
                    .emailHash(emailHash)
                    .emailEncrypted(dataEncryptor.encrypt(email))
                    .password(encodedPassword)
                    .name(dataEncryptor.encrypt(name))
                    .sex(gender)
                    .birthday(encryptedBirthday)
                    .phoneNumber(encryptedPhoneNumber)
                    .role(memberType)
                    .status(memberStatus)
                    .isSeller(isSeller)
                    .point(0)
                    .build();

                return memberRepository.save(member);
            });
    }

    private void createDeliveryAddressesIfNotExists(Member member, String memberName) {
        long count = deliveryAddressRepository.countByMemberId(member.getId());

        if (count > 0) {
            return;
        }

        for (int i = 1; i <= DELIVERY_ADDRESS_COUNT_PER_MEMBER; i++) {
            DeliveryAddress deliveryAddress = DeliveryAddress.builder()
                .memberId(member.getId())
                .label(memberName + "의 배송지" + i)
                .recipientName(dataEncryptor.encrypt(memberName))
                .recipientPhone(encryptedRecipientPhone)
                .zipCode(encryptedZipCode)
                .roadAddress(dataEncryptor.encrypt("서울특별시 강남구 테헤란로 " + i))
                .jibunAddress(dataEncryptor.encrypt("서울특별시 강남구 역삼동 " + i))
                .detailAddress(dataEncryptor.encrypt(i + "층"))
                .extraAddress(dataEncryptor.encrypt("테스트 건물"))
                .deliveryMemo(dataEncryptor.encrypt("문 앞에 놓아주세요."))
                .isDefault(i == 1)
                .build();

            deliveryAddressRepository.save(deliveryAddress);
        }
    }

    private Seller createSellerIfNotExists(
        Member member,
        String memberName,
        SellerStatus sellerStatus,
        int number
    ) {
        return sellerRepository.findById(member.getId())
            .orElseGet(() -> {
                String businessName = memberName + "의 마켓";

                Seller seller = Seller.builder()
                    .member(member)
                    .businessName(businessName)
                    .businessNumber(String.format("12345%05d", number))
                    .mailOrderRegistrationNumber("2026-서울강남-" + String.format("%04d", number))
                    .ownerName(memberName)
                    .phoneNumber("01012345678")
                    .sellerStatus(sellerStatus)
                    .approvedAt(
                        sellerStatus == SellerStatus.APPROVED
                            ? LocalDateTime.now()
                            : null
                    )
                    .bankName(encryptedBankName)
                    .bankAccount(encryptedBankAccount)
                    .depositorName(dataEncryptor.encrypt(memberName))
                    .build();

                return sellerRepository.save(seller);
            });
    }

    private void createStoresIfNotExists(Seller seller, String memberName) {
        String businessName = memberName + "의 마켓";

        long storeCount = storeRepository.countBySellerIdAndDeletedAtIsNull(seller.getId());

        if (storeCount > 0) {
            return;
        }

        for (int i = 1; i <= STORE_COUNT_PER_SELLER; i++) {
            String storeName = businessName + "의 상점" + i;

            Store store = Store.builder()
                .sellerId(seller.getId())
                .storeName(storeName)
                .phoneNumber("01012345678")
                .storeStatus(StoreStatus.OPEN)
                .build();

            Store savedStore = storeRepository.save(store);

            createStoreAddressesIfNotExists(savedStore, storeName);
        }
    }

    private void createStoreAddressesIfNotExists(Store store, String storeName) {
        long count = storeAddressRepository.countByStoreIdAndDeletedAtIsNull(store.getId());

        if (count > 0) {
            return;
        }

        AddressType[] addressTypes = {
            AddressType.SHIPPING,
            AddressType.RETURN,
            AddressType.BUSINESS
        };

        for (int i = 1; i <= STORE_ADDRESS_COUNT_PER_STORE; i++) {
            AddressType addressType = addressTypes[(i - 1) % addressTypes.length];

            StoreAddress storeAddress = StoreAddress.builder()
                .storeId(store.getId())
                .addressType(addressType)
                .label(storeName + "의 주소" + i)
                .addressPhoneNumber("01012345678")
                .zipCode("12345")
                .roadAddress("서울특별시 강남구 테헤란로 " + i)
                .jibunAddress("서울특별시 강남구 역삼동 " + i)
                .detailAddress(i + "층")
                .extraAddress("테스트 건물")
                .isDefault(i == 1)
                .build();

            storeAddressRepository.save(storeAddress);
        }
    }

    private void createAdminIfNotExists(
        String email,
        String name,
        AdminRole role,
        AdminStatus status
    ) {
        String emailHash = emailHashEncoder.encode(email);

        if (adminRepository.existsByEmailHash(emailHash)) {
            return;
        }

        Admin admin = Admin.builder()
            .emailHash(emailHash)
            .emailEncrypted(dataEncryptor.encrypt(email))
            .password(encodedPassword)
            .name(dataEncryptor.encrypt(name))
            .adminRole(role)
            .adminStatus(status)
            .build();

        adminRepository.save(admin);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
