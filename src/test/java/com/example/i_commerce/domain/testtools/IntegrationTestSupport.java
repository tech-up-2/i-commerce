package com.example.i_commerce.domain.testtools;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.StoreAddress;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
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
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

public class IntegrationTestSupport {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private StoreAddressRepository storeAddressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailHashEncoder emailHashEncoder;

    @Autowired
    private DataEncryptor dataEncryptor;

    /*
    Member
     */
    //Male
    public CustomUserPrincipal loginAsActiveMaleMember() {
        return loginAsMember(MemberStatus.ACTIVE, Gender.MALE);
    }

    public CustomUserPrincipal loginAsInactiveMaleMember() {
        return loginAsMember(MemberStatus.INACTIVE, Gender.MALE);
    }

    public CustomUserPrincipal loginAsSuspendedMaleMember() {
        return loginAsMember(MemberStatus.SUSPENDED, Gender.MALE);
    }

    public CustomUserPrincipal loginAsWithdrawnMaleMember() {
        return loginAsMember(MemberStatus.WITHDRAWN, Gender.MALE);
    }

    //Female
    public CustomUserPrincipal loginAsActiveFemaleMember() {
        return loginAsMember(MemberStatus.ACTIVE, Gender.FEMALE);
    }

    public CustomUserPrincipal loginAsInactiveFemaleMember() {
        return loginAsMember(MemberStatus.INACTIVE, Gender.FEMALE);
    }

    public CustomUserPrincipal loginAsSuspendedFemaleMember() {
        return loginAsMember(MemberStatus.SUSPENDED, Gender.FEMALE);
    }

    public CustomUserPrincipal loginAsWithdrawnFemaleMember() {
        return loginAsMember(MemberStatus.WITHDRAWN, Gender.FEMALE);
    }

    /*
    Seller
     */
    public CustomUserPrincipal loginAsPendingSeller() {
        return loginAsSeller(SellerStatus.PENDING, false);
    }

    public CustomUserPrincipal loginAsApprovedSeller() {
        return loginAsSeller(SellerStatus.APPROVED, true);
    }

    public CustomUserPrincipal loginAsBlockedSellerBeforeApproval() {
        return loginAsSeller(SellerStatus.BLOCKED, false);
    }

    public CustomUserPrincipal loginAsBlockedSellerAfterApproval() {
        return loginAsSeller(SellerStatus.BLOCKED, true);
    }

    public CustomUserPrincipal loginAsWithdrawSellerBeforeApproval() {
        return loginAsSeller(SellerStatus.WITHDRAW, false);
    }

    public CustomUserPrincipal loginAsWithdrawSellerAfterApproval() {
        return loginAsSeller(SellerStatus.WITHDRAW, true);
    }

    /*
    Store
     */
    public Store closeStore(Long sellerId) {
        return createStore(sellerId, StoreStatus.CLOSE);
    }

    public Store openStore(Long sellerId) {
        return createStore(sellerId, StoreStatus.OPEN);
    }

    public Store blockedStore(Long sellerId) {
        return createStore(sellerId, StoreStatus.BLOCKED);
    }

    public Store withdrawStore(Long sellerId) {
        return createStore(sellerId, StoreStatus.WITHDRAW);
    }

    /*
    Admin
     */
    //Master
    public CustomUserPrincipal loginAsActiveMaster() {
        return loginAsAdmin(AdminRole.MASTER, AdminStatus.ACTIVE);
    }

    public CustomUserPrincipal loginAsSuspendedMaster() {
        return loginAsAdmin(AdminRole.MASTER, AdminStatus.SUSPENDED);
    }

    public CustomUserPrincipal loginAsWithdrawMaster() {
        return loginAsAdmin(AdminRole.MASTER, AdminStatus.WITHDRAWN);
    }

    //Admin
    public CustomUserPrincipal loginAsActiveAdmin() {
        return loginAsAdmin(AdminRole.ADMIN, AdminStatus.ACTIVE);
    }

    public CustomUserPrincipal loginAsSuspendedAdmin() {
        return loginAsAdmin(AdminRole.ADMIN, AdminStatus.SUSPENDED);
    }

    public CustomUserPrincipal loginAsWithdrawAdmin() {
        return loginAsAdmin(AdminRole.ADMIN, AdminStatus.WITHDRAWN);
    }

    //Operator
    public CustomUserPrincipal loginAsActiveOperator() {
        return loginAsAdmin(AdminRole.OPERATOR, AdminStatus.ACTIVE);
    }

    public CustomUserPrincipal loginAsSuspendedOperator() {
        return loginAsAdmin(AdminRole.OPERATOR, AdminStatus.SUSPENDED);
    }

    public CustomUserPrincipal loginAsWithdrawOperator() {
        return loginAsAdmin(AdminRole.OPERATOR, AdminStatus.WITHDRAWN);
    }

    /*
 DeliveryAddress
 */
    public DeliveryAddress createDefaultDeliveryAddress(Long memberId) {
        return createDeliveryAddress(memberId, true);
    }

    public DeliveryAddress createNormalDeliveryAddress(Long memberId) {
        return createDeliveryAddress(memberId, false);
    }

    private DeliveryAddress createDeliveryAddress(Long memberId, boolean isDefault) {
        DeliveryAddress address = DeliveryAddressFixture.createDeliveryAddress(memberId, isDefault,
            dataEncryptor);
        return deliveryAddressRepository.save(address);
    }

    //사용자 지정 라벨
    public DeliveryAddress createDefaultDeliveryAddress(Long memberId, String label) {
        return createDeliveryAddress(memberId, label, true);
    }

    public DeliveryAddress createNormalDeliveryAddress(Long memberId, String label) {
        return createDeliveryAddress(memberId, label, false);
    }

    private DeliveryAddress createDeliveryAddress(Long memberId, String label, boolean isDefault) {
        DeliveryAddress address = DeliveryAddressFixture.createDeliveryAddress(label, memberId,
            isDefault,
            dataEncryptor);
        return deliveryAddressRepository.save(address);
    }

    /*
 StoreAddress
 */
    public StoreAddress createDefaultStoreAddress(Long storeId) {
        return createStoreAddress(storeId, true);
    }

    public StoreAddress createNormalStoreAddress(Long storeId) {
        return createStoreAddress(storeId, false);
    }

    private StoreAddress createStoreAddress(Long storeId, boolean isDefault) {
        StoreAddress address = StoreAddressFixture.createStoreAddress(storeId, isDefault);
        return storeAddressRepository.save(address);
    }

    //사용자 지정 라벨
    public StoreAddress createDefaultStoreAddress(Long storeId, String label) {
        return createStoreAddress(storeId, label, true);
    }

    public StoreAddress createNormalStoreAddress(Long storeId, String label) {
        return createStoreAddress(storeId, label, false);
    }

    private StoreAddress createStoreAddress(Long storeId, String label, boolean isDefault) {
        StoreAddress address = StoreAddressFixture.createStoreAddress(label, storeId, isDefault);
        return storeAddressRepository.save(address);
    }

    private CustomUserPrincipal loginAsMember(MemberStatus status, Gender gender) {
        Member member = MemberFixture.createMember(
            status,
            gender,
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        Member savedMember = memberRepository.save(member);

        return new CustomUserPrincipal(
            PrincipalType.MEMBER,
            savedMember.getId(),
            List.of(
                new SimpleGrantedAuthority("ROLE_MEMBER"),
                new SimpleGrantedAuthority("STATUS_" + status.name()),
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
            )
        );
    }

    private CustomUserPrincipal loginAsSeller(SellerStatus status, boolean approved) {
        Member member = MemberFixture.createSellerMember(
            passwordEncoder,
            emailHashEncoder,
            dataEncryptor
        );

        Member savedMember = memberRepository.save(member);

        Seller seller = SellerFixture.createSeller(
            savedMember,
            status,
            approved,
            dataEncryptor
        );

        sellerRepository.save(seller);

        return new CustomUserPrincipal(
            PrincipalType.MEMBER,
            savedMember.getId(),
            List.of(
                new SimpleGrantedAuthority("ROLE_MEMBER"),
                new SimpleGrantedAuthority("STATUS_ACTIVE"),
                new SimpleGrantedAuthority("ROLE_SELLER"),
                new SimpleGrantedAuthority("SELLER_" + status.name())
            )
        );
    }

    private Store createStore(Long sellerId, StoreStatus storeStatus) {
        Store store = StoreFixture.createStore(sellerId, storeStatus);
        return storeRepository.save(store);
    }

    private CustomUserPrincipal loginAsAdmin(AdminRole adminRole, AdminStatus adminStatus) {
        Admin admin = AdminFixture.createAdmin(adminRole, adminStatus,
            passwordEncoder, emailHashEncoder, dataEncryptor);

        Admin savedAdmin = adminRepository.save(admin);

        return new CustomUserPrincipal(
            PrincipalType.ADMIN,
            savedAdmin.getId(),
            List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("STATUS_" + adminStatus.name()),
                new SimpleGrantedAuthority("ADMIN_" + adminRole.name())
            )
        );
    }
}
