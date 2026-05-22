package com.example.i_commerce.domain.member.service.admin;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.service.admin.dto.AdminCreateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminCreateResponse;
import com.example.i_commerce.domain.member.service.admin.dto.AdminInfoResponse;
import com.example.i_commerce.domain.member.service.admin.dto.AdminLoginResponse;
import com.example.i_commerce.domain.member.service.admin.dto.AdminMemberResponse;
import com.example.i_commerce.domain.member.service.admin.dto.AdminMemberStatusUpdateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminRoleUpdateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminSellerResponse;
import com.example.i_commerce.domain.member.service.admin.dto.AdminSellerStatusUpdateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminStatusUpdateRequest;
import com.example.i_commerce.domain.member.service.admin.dto.AdminUpdateResponse;
import com.example.i_commerce.domain.member.service.auth.dto.LoginRequest;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
import com.example.i_commerce.global.common.response.SliceResponse;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.BlacklistedTokenRepository;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.TokenHashUtil;
import com.example.i_commerce.global.security.jwt.TokenPayload;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final DataEncryptor dataEncryptor;
    private final PasswordEncoder passwordEncoder;
    private final EmailHashEncoder emailHashEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final TokenHashUtil tokenHashUtil;

    @Transactional(readOnly = true)
    public AdminLoginResponse login(LoginRequest dto) {
        Admin admin = adminRepository.findByEmailHash(emailHashEncoder.encode(dto.email()))
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.password(), admin.getPassword())) {
            throw new AppException(MemberErrorCode.INVALID_PASSWORD);
        }

        validateLoginStatus(admin);// status상태 검즘

        String email = dataEncryptor.decrypt(admin.getEmailEncrypted());

        TokenPayload payload = new TokenPayload(
            PrincipalType.ADMIN,
            admin.getId(),
            admin.getAdminRole(),
            admin.getAdminStatus(),
            null
        );

        String accessToken = jwtTokenUtil.createToken(payload);

        return new AdminLoginResponse(
            admin.getId(),
            accessToken
        );
    }

    private void validateLoginStatus(Admin admin) {
        switch (admin.getAdminStatus()) {
            case WITHDRAWN -> throw new AppException(MemberErrorCode.WITHDRAWN_MEMBER);
        }
    }

    //관리자 생성
    @Transactional
    public AdminCreateResponse createAdmin(AdminCreateRequest request) {
        String emailHash = emailHashEncoder.encode(request.email());

        if (adminRepository.existsByEmailHash(emailHash)) {
            throw new AppException(MemberErrorCode.DUPLICATED_EMAIL);
        }

        Admin admin = Admin.builder()
            .emailHash(emailHash)
            .emailEncrypted(dataEncryptor.encrypt(request.email()))
            .password(passwordEncoder.encode(request.password()))
            .name(dataEncryptor.encrypt(request.name()))
            .adminRole(request.adminRole())
            .adminStatus(AdminStatus.ACTIVE)
            .build();

        Admin savedAdmin = adminRepository.save(admin);

        return new AdminCreateResponse(
            savedAdmin.getId(),
            dataEncryptor.decrypt(savedAdmin.getEmailEncrypted()),
            dataEncryptor.decrypt(savedAdmin.getName()),
            savedAdmin.getAdminRole(),
            savedAdmin.getAdminStatus()
        );
    }

    //관리자 목록 조회
    @Transactional(readOnly = true)
    public SliceResponse<AdminInfoResponse> getAdmins(Pageable pageable) {
        Slice<Admin> admins = adminRepository.findAllByDeletedAtIsNull(pageable);

        return SliceResponse.of(admins, this::toAdminInfoResponse);
    }

    private AdminInfoResponse toAdminInfoResponse(Admin admin) {
        return new AdminInfoResponse(
            admin.getId(),
            dataEncryptor.decrypt(admin.getEmailEncrypted()),
            dataEncryptor.decrypt(admin.getName()),
            admin.getAdminRole(),
            admin.getAdminStatus(),
            admin.getCreatedAt(),
            admin.getUpdatedAt()
        );
    }

    //관리자 권한 변경
    @Transactional
    public AdminUpdateResponse updateAdminRole(
        Long adminId,
        AdminRoleUpdateRequest request
    ) {
        Admin admin = findActiveAdmin(adminId);

        validateAtLeastOneActiveMasterAfterRoleChange(admin, request.adminRole());

        admin.changeRole(request.adminRole());

        return toAdminUpdateResponse(admin);
    }

    //관리자 상태 변경
    @Transactional
    public AdminUpdateResponse updateAdminStatus(
        Long adminId,
        AdminStatusUpdateRequest request
    ) {
        Admin admin = findActiveAdmin(adminId);

        validateAtLeastOneActiveMasterAfterStatusChange(admin, request.adminStatus());

        admin.changeStatus(request.adminStatus());

        return toAdminUpdateResponse(admin);
    }

    private Admin findActiveAdmin(Long adminId) {
        return adminRepository.findByIdAndDeletedAtIsNull(adminId)
            .orElseThrow(() -> new AppException(MemberErrorCode.ADMIN_NOT_FOUND));
    }

    private void validateAtLeastOneActiveMasterAfterRoleChange(
        Admin admin,
        AdminRole newRole
    ) {
        boolean targetIsCurrentlyActiveMaster =
            admin.getAdminRole() == AdminRole.MASTER
                && admin.getAdminStatus() == AdminStatus.ACTIVE;

        boolean roleWillNoLongerBeMaster = newRole != AdminRole.MASTER;

        if (targetIsCurrentlyActiveMaster && roleWillNoLongerBeMaster) {
            validateMoreThanOneActiveMasterExists();
        }
    }

    private void validateAtLeastOneActiveMasterAfterStatusChange(
        Admin admin,
        AdminStatus newStatus
    ) {
        boolean targetIsCurrentlyActiveMaster =
            admin.getAdminRole() == AdminRole.MASTER
                && admin.getAdminStatus() == AdminStatus.ACTIVE;

        boolean statusWillNoLongerBeActive = newStatus != AdminStatus.ACTIVE;

        if (targetIsCurrentlyActiveMaster && statusWillNoLongerBeActive) {
            validateMoreThanOneActiveMasterExists();
        }
    }

    private void validateMoreThanOneActiveMasterExists() {
        long activeMasterCount =
            adminRepository.countByAdminRoleAndAdminStatusAndDeletedAtIsNull(
                AdminRole.MASTER,
                AdminStatus.ACTIVE
            );

        if (activeMasterCount <= 1) {
            throw new AppException(MemberErrorCode.LAST_ACTIVE_MASTER_REQUIRED);
        }
    }

    private AdminUpdateResponse toAdminUpdateResponse(Admin admin) {
        return new AdminUpdateResponse(
            admin.getId(),
            dataEncryptor.decrypt(admin.getEmailEncrypted()),
            dataEncryptor.decrypt(admin.getName()),
            admin.getAdminRole(),
            admin.getAdminStatus()
        );
    }

    //관리자 로그인 이력 관리
    //관리자-사용자 관리
    @Transactional(readOnly = true)
    public AdminMemberResponse getMember(Long userId) {
        Member member = findMember(userId);
        return toAdminMemberResponse(member);
    }

    @Transactional
    public AdminMemberResponse updateMemberStatus(
        Long userId,
        AdminMemberStatusUpdateRequest request
    ) {
        Member member = findMember(userId);

        member.changeStatus(request.memberStatus());

        return toAdminMemberResponse(member);
    }

    //관리자-판매자 관리
    @Transactional(readOnly = true)
    public AdminSellerResponse getSeller(Long sellerId) {
        Seller seller = findSellerWithMember(sellerId);
        return toAdminSellerResponse(seller);
    }

    @Transactional
    public AdminSellerResponse updateSellerStatus(
        Long sellerId,
        AdminSellerStatusUpdateRequest request
    ) {
        Seller seller = findSellerWithMember(sellerId);

        if (request.sellerStatus() == SellerStatus.APPROVED) {
            seller.approve();
        } else {
            seller.changeStatus(request.sellerStatus());
        }

        return toAdminSellerResponse(seller);
    }

    private Member findMember(Long userId) {
        return memberRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new AppException(MemberErrorCode.USER_NOT_FOUND));
    }

    private Seller findSellerWithMember(Long sellerId) {
        return sellerRepository.findByIdAndDeletedAtIsNull(sellerId)
            .orElseThrow(() -> new AppException(MemberErrorCode.SELLER_NOT_FOUND));
    }

    private AdminMemberResponse toAdminMemberResponse(Member member) {
        return new AdminMemberResponse(
            member.getId(),
            dataEncryptor.decrypt(member.getEmailEncrypted()),
            dataEncryptor.decrypt(member.getName()),
            member.getRole(),
            member.getStatus(),
            member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }

    private AdminSellerResponse toAdminSellerResponse(Seller seller) {
        Member member = seller.getMember();

        return new AdminSellerResponse(
            seller.getId(),
            dataEncryptor.decrypt(member.getEmailEncrypted()),
            seller.getBusinessName(),
            seller.getBusinessNumber(),
            seller.getOwnerName(),
            member.getStatus(),
            seller.getSellerStatus(),
            seller.getCreatedAt(),
            seller.getUpdatedAt()
        );
    }
}
