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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    private static final int MEMBER_COUNT_PER_STATUS = 15000;
    private static final int SELLER_COUNT_PER_STATUS = 1000;
    private static final int ADMIN_COUNT_PER_ROLE = 100;

    private static final int DELIVERY_ADDRESS_COUNT_PER_MEMBER = 2;
    private static final int STORE_COUNT_PER_SELLER = 2;
    private static final int STORE_ADDRESS_COUNT_PER_STORE = 3;

    private static final int BATCH_SIZE = 1000;

    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;
    private final StoreRepository storeRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final StoreAddressRepository storeAddressRepository;
    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;
    private final EmailHashEncoder emailHashEncoder;
    private final DataEncryptor dataEncryptor;

    @PersistenceContext
    private EntityManager entityManager;

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

        initCommonValues();

        createMembersFast();
        createSellersFast();
        createAdminsFast();

        log.info("더미 데이터 생성이 완료되었습니다.");
    }

    private void initCommonValues() {
        this.encodedPassword = passwordEncoder.encode(PASSWORD);
        this.encryptedBirthday = dataEncryptor.encrypt(LocalDate.of(1999, 1, 1).toString());
        this.encryptedPhoneNumber = dataEncryptor.encrypt("01012345678");
        this.encryptedZipCode = dataEncryptor.encrypt("12345");
        this.encryptedRecipientPhone = dataEncryptor.encrypt("01012345678");
        this.encryptedBankName = dataEncryptor.encrypt("국민은행");
        this.encryptedBankAccount = dataEncryptor.encrypt("12345678901234");
    }

    private void createMembersFast() {
        log.info("일반 회원 더미 데이터 생성을 시작합니다.");

        List<MemberSeed> seeds = new ArrayList<>(BATCH_SIZE);

        for (MemberStatus status : MemberStatus.values()) {
            for (int i = 1; i <= MEMBER_COUNT_PER_STATUS; i++) {
                String email = status.name().toLowerCase() + "Member" + i + "@test.com";
                String emailHash = emailHashEncoder.encode(email);
                String name = "일반회원" + i;

                seeds.add(new MemberSeed(
                    email,
                    emailHash,
                    name,
                    MemberType.CUSTOMER,
                    status,
                    false,
                    i
                ));

                if (seeds.size() >= BATCH_SIZE) {
                    saveMemberSeeds(seeds);
                    seeds.clear();
                }
            }
        }

        if (!seeds.isEmpty()) {
            saveMemberSeeds(seeds);
            seeds.clear();
        }

        log.info("일반 회원 더미 데이터 생성을 완료했습니다.");
    }

    private void saveMemberSeeds(List<MemberSeed> seeds) {
        Set<String> existingEmailHashes = findExistingMemberEmailHashes(seeds);

        List<MemberSeed> newSeeds = seeds.stream()
            .filter(seed -> !existingEmailHashes.contains(seed.emailHash()))
            .toList();

        if (newSeeds.isEmpty()) {
            return;
        }

        List<Member> members = new ArrayList<>(newSeeds.size());

        for (MemberSeed seed : newSeeds) {
            members.add(createMember(seed));
        }

        memberRepository.saveAll(members);
        memberRepository.flush();

        List<DeliveryAddress> deliveryAddresses = new ArrayList<>(
            members.size() * DELIVERY_ADDRESS_COUNT_PER_MEMBER
        );

        for (int idx = 0; idx < members.size(); idx++) {
            Member member = members.get(idx);
            MemberSeed seed = newSeeds.get(idx);

            for (int i = 1; i <= DELIVERY_ADDRESS_COUNT_PER_MEMBER; i++) {
                deliveryAddresses.add(createDeliveryAddress(member.getId(), seed.name(), i));
            }
        }

        deliveryAddressRepository.saveAll(deliveryAddresses);
        deliveryAddressRepository.flush();

        entityManager.clear();
    }

    private void createSellersFast() {
        log.info("판매자 더미 데이터 생성을 시작합니다.");

        List<SellerSeed> seeds = new ArrayList<>(BATCH_SIZE);

        for (SellerStatus sellerStatus : SellerStatus.values()) {
            for (int i = 1; i <= SELLER_COUNT_PER_STATUS; i++) {
                String email = sellerStatus.name().toLowerCase() + "Seller" + i + "@test.com";
                String emailHash = emailHashEncoder.encode(email);
                String name = "판매자" + i;

                seeds.add(new SellerSeed(
                    email,
                    emailHash,
                    name,
                    sellerStatus,
                    i
                ));

                if (seeds.size() >= BATCH_SIZE) {
                    saveSellerSeeds(seeds);
                    seeds.clear();
                }
            }
        }

        if (!seeds.isEmpty()) {
            saveSellerSeeds(seeds);
            seeds.clear();
        }

        log.info("판매자 더미 데이터 생성을 완료했습니다.");
    }

    private void saveSellerSeeds(List<SellerSeed> seeds) {
        Set<String> existingEmailHashes = findExistingMemberEmailHashesForSellers(seeds);

        List<SellerSeed> newSeeds = seeds.stream()
            .filter(seed -> !existingEmailHashes.contains(seed.emailHash()))
            .toList();

        if (newSeeds.isEmpty()) {
            return;
        }

        List<Member> sellerMembers = new ArrayList<>(newSeeds.size());

        for (SellerSeed seed : newSeeds) {
            MemberSeed memberSeed = new MemberSeed(
                seed.email(),
                seed.emailHash(),
                seed.name(),
                MemberType.SELLER,
                MemberStatus.ACTIVE,
                true,
                seed.number()
            );

            sellerMembers.add(createMember(memberSeed));
        }

        memberRepository.saveAll(sellerMembers);
        memberRepository.flush();

        List<Seller> sellers = new ArrayList<>(newSeeds.size());

        for (int idx = 0; idx < sellerMembers.size(); idx++) {
            Member member = sellerMembers.get(idx);
            SellerSeed seed = newSeeds.get(idx);

            sellers.add(createSeller(member, seed.name(), seed.sellerStatus(), seed.number()));
        }

        sellerRepository.saveAll(sellers);
        sellerRepository.flush();

        List<Store> stores = new ArrayList<>(sellers.size() * STORE_COUNT_PER_SELLER);

        for (int idx = 0; idx < sellers.size(); idx++) {
            Seller seller = sellers.get(idx);
            SellerSeed seed = newSeeds.get(idx);

            String businessName = seed.name() + "의 마켓";

            for (int i = 1; i <= STORE_COUNT_PER_SELLER; i++) {
                String storeName = businessName + "의 상점" + i;

                Store store = Store.builder()
                    .sellerId(seller.getId())
                    .storeName(storeName)
                    .phoneNumber("01012345678")
                    .storeStatus(StoreStatus.OPEN)
                    .build();

                stores.add(store);
            }
        }

        storeRepository.saveAll(stores);
        storeRepository.flush();

        List<StoreAddress> storeAddresses = new ArrayList<>(
            stores.size() * STORE_ADDRESS_COUNT_PER_STORE
        );

        for (Store store : stores) {
            for (int i = 1; i <= STORE_ADDRESS_COUNT_PER_STORE; i++) {
                storeAddresses.add(createStoreAddress(store.getId(), store.getStoreName(), i));
            }
        }

        storeAddressRepository.saveAll(storeAddresses);
        storeAddressRepository.flush();

        entityManager.clear();
    }

    private void createAdminsFast() {
        log.info("관리자 더미 데이터 생성을 시작합니다.");

        List<AdminSeed> seeds = new ArrayList<>(BATCH_SIZE);

        for (AdminStatus status : AdminStatus.values()) {
            for (AdminRole role : AdminRole.values()) {
                for (int i = 1; i <= ADMIN_COUNT_PER_ROLE; i++) {
                    String email = status.name().toLowerCase()
                        + capitalize(role.name().toLowerCase())
                        + i
                        + "@test.com";

                    String emailHash = emailHashEncoder.encode(email);
                    String name = "관리자" + i;

                    seeds.add(new AdminSeed(
                        email,
                        emailHash,
                        name,
                        role,
                        status
                    ));

                    if (seeds.size() >= BATCH_SIZE) {
                        saveAdminSeeds(seeds);
                        seeds.clear();
                    }
                }
            }
        }

        if (!seeds.isEmpty()) {
            saveAdminSeeds(seeds);
            seeds.clear();
        }

        log.info("관리자 더미 데이터 생성을 완료했습니다.");
    }

    private void saveAdminSeeds(List<AdminSeed> seeds) {
        List<String> emailHashes = seeds.stream()
            .map(AdminSeed::emailHash)
            .toList();

        Set<String> existingEmailHashes = adminRepository.findAllByEmailHashIn(emailHashes)
            .stream()
            .map(Admin::getEmailHash)
            .collect(Collectors.toSet());

        List<Admin> admins = new ArrayList<>();

        for (AdminSeed seed : seeds) {
            if (existingEmailHashes.contains(seed.emailHash())) {
                continue;
            }

            Admin admin = Admin.builder()
                .emailHash(seed.emailHash())
                .emailEncrypted(dataEncryptor.encrypt(seed.email()))
                .password(encodedPassword)
                .name(dataEncryptor.encrypt(seed.name()))
                .adminRole(seed.role())
                .adminStatus(seed.status())
                .build();

            admins.add(admin);
        }

        if (admins.isEmpty()) {
            return;
        }

        adminRepository.saveAll(admins);
        adminRepository.flush();

        entityManager.clear();
    }

    private Set<String> findExistingMemberEmailHashes(List<MemberSeed> seeds) {
        List<String> emailHashes = seeds.stream()
            .map(MemberSeed::emailHash)
            .toList();

        return memberRepository.findAllByEmailHashIn(emailHashes)
            .stream()
            .map(Member::getEmailHash)
            .collect(Collectors.toSet());
    }

    private Set<String> findExistingMemberEmailHashesForSellers(List<SellerSeed> seeds) {
        List<String> emailHashes = seeds.stream()
            .map(SellerSeed::emailHash)
            .toList();

        return memberRepository.findAllByEmailHashIn(emailHashes)
            .stream()
            .map(Member::getEmailHash)
            .collect(Collectors.toSet());
    }

    private Member createMember(MemberSeed seed) {
        Gender gender = seed.number() % 2 == 0
            ? Gender.FEMALE
            : Gender.MALE;

        return Member.builder()
            .emailHash(seed.emailHash())
            .emailEncrypted(dataEncryptor.encrypt(seed.email()))
            .password(encodedPassword)
            .name(dataEncryptor.encrypt(seed.name()))
            .sex(gender)
            .birthday(encryptedBirthday)
            .phoneNumber(encryptedPhoneNumber)
            .role(seed.memberType())
            .status(seed.memberStatus())
            .isSeller(seed.isSeller())
            .point(0)
            .build();
    }

    private DeliveryAddress createDeliveryAddress(Long memberId, String memberName, int number) {
        return DeliveryAddress.builder()
            .memberId(memberId)
            .label(memberName + "의 배송지" + number)
            .recipientName(dataEncryptor.encrypt(memberName))
            .recipientPhone(encryptedRecipientPhone)
            .zipCode(encryptedZipCode)
            .roadAddress(dataEncryptor.encrypt("서울특별시 강남구 테헤란로 " + number))
            .jibunAddress(dataEncryptor.encrypt("서울특별시 강남구 역삼동 " + number))
            .detailAddress(dataEncryptor.encrypt(number + "층"))
            .extraAddress(dataEncryptor.encrypt("테스트 건물"))
            .deliveryMemo(dataEncryptor.encrypt("문 앞에 놓아주세요."))
            .isDefault(number == 1)
            .build();
    }

    private Seller createSeller(
        Member member,
        String memberName,
        SellerStatus sellerStatus,
        int number
    ) {
        String businessName = memberName + "의 마켓";

        return Seller.builder()
            .member(member)
            .businessName(businessName)
            .businessNumber(generateBusinessNumber(sellerStatus, number))
            .mailOrderRegistrationNumber(generateMailOrderRegistrationNumber(sellerStatus, number))
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
    }

    private StoreAddress createStoreAddress(Long storeId, String storeName, int number) {
        AddressType[] addressTypes = {
            AddressType.SHIPPING,
            AddressType.RETURN,
            AddressType.BUSINESS
        };

        return StoreAddress.builder()
            .storeId(storeId)
            .addressType(addressTypes[(number - 1) % addressTypes.length])
            .label(storeName + "의 주소" + number)
            .addressPhoneNumber("01012345678")
            .zipCode("12345")
            .roadAddress("서울특별시 강남구 테헤란로 " + number)
            .jibunAddress("서울특별시 강남구 역삼동 " + number)
            .detailAddress(number + "층")
            .extraAddress("테스트 건물")
            .isDefault(number == 1)
            .build();
    }

    private String generateBusinessNumber(SellerStatus status, int number) {
        int prefix = switch (status) {
            case PENDING -> 10;
            case APPROVED -> 20;
            case BLOCKED -> 30;
            case WITHDRAW -> 40;
        };

        return String.format("%02d%08d", prefix, number);
    }

    private String generateMailOrderRegistrationNumber(SellerStatus status, int number) {
        return "2026-서울강남-"
            + status.name().toLowerCase()
            + "-"
            + String.format("%04d", number);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private record MemberSeed(
        String email,
        String emailHash,
        String name,
        MemberType memberType,
        MemberStatus memberStatus,
        boolean isSeller,
        int number
    ) {

    }

    private record SellerSeed(
        String email,
        String emailHash,
        String name,
        SellerStatus sellerStatus,
        int number
    ) {

    }

    private record AdminSeed(
        String email,
        String emailHash,
        String name,
        AdminRole role,
        AdminStatus status
    ) {

    }
}
