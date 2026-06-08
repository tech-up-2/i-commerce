package com.example.i_commerce.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.i_commerce.domain.member.entity.Admin;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.entity.enums.LoginFailReason;
import com.example.i_commerce.domain.member.entity.enums.LoginResult;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.domain.member.repository.AdminLoginHistoryRepository;
import com.example.i_commerce.domain.member.repository.AdminRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.service.admin.AdminService;
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
import com.example.i_commerce.domain.testtools.IntegrationTestSupport;
import com.example.i_commerce.global.common.response.SliceResponse;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "app.bootstrap.admin.enabled=${ADMIN_ENABLED:true}",
    "app.bootstrap.admin.email=${ADMIN_EMAIL:master@test.com}",
    "app.bootstrap.admin.password=${ADMIN_PASSWORD:master123!}",
    "app.bootstrap.admin.name=최초관리자"
}, locations = "file:.env")
class AdminManagementIntegrationTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EmailHashEncoder emailHashEncoder;

    @Autowired
    private DataEncryptor dataEncryptor;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private AdminLoginHistoryRepository adminLoginHistoryRepository;

    @Test
    @DisplayName("서버 시작 시 최초 MASTER 관리자가 자동 생성된다")
    void bootstrapMasterAdmin_success() {
        String masterEmailHash = emailHashEncoder.encode("master@test.com");

        Admin master = adminRepository.findByEmailHash(masterEmailHash)
            .orElseThrow();

        assertThat(master.getAdminRole()).isEqualTo(AdminRole.MASTER);
        assertThat(master.getAdminStatus()).isEqualTo(AdminStatus.ACTIVE);
        assertThat(passwordEncoder.matches("master123!", master.getPassword())).isTrue();

        String email = dataEncryptor.decrypt(master.getEmailEncrypted());
        String name = dataEncryptor.decrypt(master.getName());

        assertThat(email).isEqualTo("master@test.com");
        assertThat(name).isEqualTo("최초관리자");
    }

    /*
    로그인 테스트
     */
    @Test
    @DisplayName("로그인 테스트")
    void adminlogin_success() throws Exception {
        LoginRequest loginRequest = new LoginRequest(
            "master@test.com",
            "master123!"
        );

        AdminLoginResponse loginResponse = adminService.login(loginRequest);

        Optional<Admin> admin = adminRepository.findById(loginResponse.memberId());

        assertAll(
            () -> assertThat(dataEncryptor.decrypt(admin.get().getEmailEncrypted())).
                isEqualTo("master@test.com"),
            () -> assertThat(loginResponse.accessToken()).isNotNull()
        );
    }

    @Test
    @DisplayName("관리자 로그인 실패 - 존재하지 않는 이메일")
    void adminLogin_fail_userNotFound() {
        // given
        LoginRequest request = new LoginRequest(
            "notfound-admin@test.com",
            "master123!"
        );

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.login(request)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.USER_NOT_FOUND);
        assertThat(adminLoginHistoryRepository.countByLoginResultAndFailReason(
            LoginResult.FAILURE,
            LoginFailReason.INVALID_CREDENTIALS
        )).isEqualTo(1L);
    }


    /*
    관리자 생성 테스트
     */

    @Test
    @DisplayName("새 관리자 생성")
    void createAdmin_success() throws Exception {
        AdminCreateRequest request_master = new AdminCreateRequest(
            "newmaster@test.com",
            "password123!",
            "새 관리자 1",
            AdminRole.MASTER
        );

        AdminCreateRequest request_admin = new AdminCreateRequest(
            "newadmin@test.com",
            "password123!",
            "새 관리자 2",
            AdminRole.ADMIN
        );

        AdminCreateRequest request_operator = new AdminCreateRequest(
            "newoperator@test.com",
            "password123!",
            "새 관리자 3",
            AdminRole.OPERATOR
        );

        AdminCreateResponse response_master = adminService.createAdmin(request_master);
        AdminCreateResponse response_admin = adminService.createAdmin(request_admin);
        AdminCreateResponse response_operator = adminService.createAdmin(request_operator);

        assertThat(response_master.name()).isEqualTo("새 관리자 1");
        assertThat(response_master.adminRole()).isEqualTo(AdminRole.MASTER);
        assertThat(response_admin.name()).isEqualTo("새 관리자 2");
        assertThat(response_admin.adminRole()).isEqualTo(AdminRole.ADMIN);
        assertThat(response_operator.name()).isEqualTo("새 관리자 3");
        assertThat(response_operator.adminRole()).isEqualTo(AdminRole.OPERATOR);
    }

    @Test
    @DisplayName("새 관리자 생성 실패")
    void createAdmin_fail() {
        AdminCreateRequest request = new AdminCreateRequest(
            "newmaster@test.com",
            "password123!",
            "새 관리자 1",
            AdminRole.MASTER
        );

        adminService.createAdmin(request);

        AdminCreateRequest request_master = new AdminCreateRequest(
            "newmaster@test.com",
            "password123!",
            "새 관리자 1",
            AdminRole.MASTER
        );

        AdminCreateRequest request_admin = new AdminCreateRequest(
            "newmaster@test.com",
            "password123!",
            "새 관리자 2",
            AdminRole.ADMIN
        );

        AdminCreateRequest request_operator = new AdminCreateRequest(
            "newmaster@test.com",
            "password123!",
            "새 관리자 3",
            AdminRole.OPERATOR
        );

        AppException exception1 = assertThrows(
            AppException.class,
            () -> adminService.createAdmin(request_master)
        );
        AppException exception2 = assertThrows(
            AppException.class,
            () -> adminService.createAdmin(request_admin)
        );
        AppException exception3 = assertThrows(
            AppException.class,
            () -> adminService.createAdmin(request_operator)
        );

        assertThat(exception1.getErrorCode()).isEqualTo(MemberErrorCode.DUPLICATED_EMAIL);
        assertThat(exception2.getErrorCode()).isEqualTo(MemberErrorCode.DUPLICATED_EMAIL);
        assertThat(exception3.getErrorCode()).isEqualTo(MemberErrorCode.DUPLICATED_EMAIL);
    }

    /*
    관리자 목록 조회 테스트
     */

    @Test
    @DisplayName("관리자 목록 조회 성공")
    void getAdmins_success() {
        // given
        Admin admin1 = adminRepository.save(Admin.builder()
            .emailHash(emailHashEncoder.encode("admin1@test.com"))
            .emailEncrypted(dataEncryptor.encrypt("admin1@test.com"))
            .password(passwordEncoder.encode("admin123!"))
            .name(dataEncryptor.encrypt("관리자1"))
            .adminRole(AdminRole.ADMIN)
            .adminStatus(AdminStatus.ACTIVE)
            .build()
        );

        Admin admin2 = adminRepository.save(Admin.builder()
            .emailHash(emailHashEncoder.encode("admin2@test.com"))
            .emailEncrypted(dataEncryptor.encrypt("admin2@test.com"))
            .password(passwordEncoder.encode("admin123!"))
            .name(dataEncryptor.encrypt("관리자2"))
            .adminRole(AdminRole.OPERATOR)
            .adminStatus(AdminStatus.ACTIVE)
            .build()
        );

        Pageable pageable = PageRequest.of(0, 10);

        // when
        SliceResponse<AdminInfoResponse> response = adminService.getAdmins(pageable);

        // then
        List<AdminInfoResponse> content = response.content();

        assertAll(
            () -> assertThat(content).hasSizeGreaterThanOrEqualTo(2),

            () -> assertThat(content)
                .extracting(AdminInfoResponse::email)
                .contains("admin1@test.com", "admin2@test.com"),

            () -> assertThat(content)
                .extracting(AdminInfoResponse::name)
                .contains("관리자1", "관리자2"),

            () -> assertThat(content)
                .extracting(AdminInfoResponse::adminRole)
                .contains(AdminRole.ADMIN, AdminRole.OPERATOR),

            () -> assertThat(content)
                .extracting(AdminInfoResponse::adminStatus)
                .contains(AdminStatus.ACTIVE)
        );
    }

    @Test
    @DisplayName("관리자 목록 조회 성공 - 삭제된 관리자는 제외된다")
    void getAdmins_success_excludeDeletedAdmin() {
        // given
        Admin activeAdmin = adminRepository.save(Admin.builder()
            .emailHash(emailHashEncoder.encode("active-admin@test.com"))
            .emailEncrypted(dataEncryptor.encrypt("active-admin@test.com"))
            .password(passwordEncoder.encode("admin123!"))
            .name(dataEncryptor.encrypt("정상관리자"))
            .adminRole(AdminRole.ADMIN)
            .adminStatus(AdminStatus.ACTIVE)
            .build()
        );

        Admin deletedAdmin = adminRepository.save(Admin.builder()
            .emailHash(emailHashEncoder.encode("deleted-admin@test.com"))
            .emailEncrypted(dataEncryptor.encrypt("deleted-admin@test.com"))
            .password(passwordEncoder.encode("admin123!"))
            .name(dataEncryptor.encrypt("삭제관리자"))
            .adminRole(AdminRole.ADMIN)
            .adminStatus(AdminStatus.WITHDRAWN)
            .build()
        );

        deletedAdmin.delete();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        SliceResponse<AdminInfoResponse> response = adminService.getAdmins(pageable);

        // then
        assertThat(response.content())
            .extracting(AdminInfoResponse::email)
            .contains("active-admin@test.com")
            .doesNotContain("deleted-admin@test.com");
    }

    /*
    관리자 권한 변경 테스트
     */

    @Test
    @DisplayName("다른 관리자의 권한 변경")
    void updateAdminRole_success() {
        // given
        AdminCreateRequest request = new AdminCreateRequest(
            "adminRole-target@test.com",
            "admin123!",
            "권한변경대상",
            AdminRole.ADMIN
        );

        AdminCreateResponse createResponse = adminService.createAdmin(request);

        AdminRoleUpdateRequest updateRequest = new AdminRoleUpdateRequest(
            AdminRole.OPERATOR
        );

        // when
        AdminUpdateResponse response = adminService.updateAdminRole(
            createResponse.adminId(),
            updateRequest
        );

        assertThat(response.adminRole()).isEqualTo(AdminRole.OPERATOR);
    }

    @Test
    @DisplayName("마지막 ACTIVE MASTER 관리자는 권한 변경할 수 없다")
    void updateLastActiveMasterRole_fail() {
        // given
        Long masterId = findAdminIdByEmail("master@test.com");

        AdminRoleUpdateRequest request = new AdminRoleUpdateRequest(AdminRole.ADMIN);
        AdminRoleUpdateRequest request2 = new AdminRoleUpdateRequest(AdminRole.OPERATOR);

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateAdminRole(masterId, request)
        );
        AppException exception2 = assertThrows(
            AppException.class,
            () -> adminService.updateAdminRole(masterId, request2)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.LAST_ACTIVE_MASTER_REQUIRED);
        assertThat(exception2.getErrorCode())
            .isEqualTo(MemberErrorCode.LAST_ACTIVE_MASTER_REQUIRED);
    }

    @Test
    @DisplayName("마지막 ACTIVE MASTER 관리자는 상태를 변경할 수 없다")
    void updateLastActiveMasterStatus_fail() {
        // given
        Long masterId = findAdminIdByEmail("master@test.com");

        AdminStatusUpdateRequest request = new AdminStatusUpdateRequest(AdminStatus.LOCKED);
        AdminStatusUpdateRequest request2 = new AdminStatusUpdateRequest(AdminStatus.WITHDRAWN);

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateAdminStatus(masterId, request)
        );
        AppException exception2 = assertThrows(
            AppException.class,
            () -> adminService.updateAdminStatus(masterId, request2)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.LAST_ACTIVE_MASTER_REQUIRED);
        assertThat(exception2.getErrorCode())
            .isEqualTo(MemberErrorCode.LAST_ACTIVE_MASTER_REQUIRED);
    }

    @Test
    @DisplayName("ACTIVE MASTER가 2명 이상이면 기존 MASTER를 ADMIN으로 변경할 수 있다")
    void updateMasterRole_success_whenAnotherActiveMasterExists() {
        // given
        Long firstMasterId = findAdminIdByEmail("master@test.com");

        AdminCreateRequest createRequest = new AdminCreateRequest(
            "master2@test.com",
            "master123!",
            "두번째마스터",
            AdminRole.MASTER
        );

        AdminCreateResponse secondMaster = adminService.createAdmin(createRequest);

        AdminRoleUpdateRequest request = new AdminRoleUpdateRequest(AdminRole.ADMIN);

        // when
        AdminUpdateResponse response = adminService.updateAdminRole(
            firstMasterId,
            request
        );

        // then
        Admin firstMaster = adminRepository.findById(firstMasterId).orElseThrow();
        Admin secondMasterAfter = adminRepository.findById(secondMaster.adminId()).orElseThrow();

        assertAll(
            () -> assertThat(response.adminId()).isEqualTo(firstMasterId),
            () -> assertThat(response.adminRole()).isEqualTo(AdminRole.ADMIN),

            () -> assertThat(firstMaster.getAdminRole()).isEqualTo(AdminRole.ADMIN),
            () -> assertThat(secondMasterAfter.getAdminRole()).isEqualTo(AdminRole.MASTER),
            () -> assertThat(secondMasterAfter.getAdminStatus()).isEqualTo(AdminStatus.ACTIVE)
        );
    }

    @Test
    @DisplayName("관리자 권한 변경 실패 - id")
    void updateAdminRole_fail_id() {
        AdminCreateRequest createRequest = new AdminCreateRequest(
            "admin@test.com",
            "admin123!",
            "테스트",
            AdminRole.ADMIN
        );

        adminService.createAdmin(createRequest);

        AdminRoleUpdateRequest request = new AdminRoleUpdateRequest(AdminRole.OPERATOR);

        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateAdminRole(null, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.ADMIN_NOT_FOUND);
    }

    @Test
    @DisplayName("관리자 권한 변경 실패 - request")
    void updateAdminRole_fail_role() {
        AdminCreateRequest createRequest = new AdminCreateRequest(
            "admin@test.com",
            "admin123!",
            "테스트",
            AdminRole.ADMIN
        );

        AdminCreateResponse createResponse = adminService.createAdmin(createRequest);

        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateAdminRole(createResponse.adminId(), null)
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_ROLE);
    }

    @Test
    @DisplayName("관리자 상태 변경 실패 - id")
    void updateAdminStatus_fail_id() {
        AdminCreateRequest createRequest = new AdminCreateRequest(
            "admin@test.com",
            "admin123!",
            "테스트",
            AdminRole.ADMIN
        );

        adminService.createAdmin(createRequest);

        AdminStatusUpdateRequest request = new AdminStatusUpdateRequest(AdminStatus.LOCKED);

        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateAdminStatus(null, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.ADMIN_NOT_FOUND);
    }

    @Test
    @DisplayName("관리자 상태 변경 실패 - request")
    void updateAdminStatus_fail_Status() {
        AdminCreateRequest createRequest = new AdminCreateRequest(
            "admin@test.com",
            "admin123!",
            "테스트",
            AdminRole.ADMIN
        );

        AdminCreateResponse createResponse = adminService.createAdmin(createRequest);

        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateAdminStatus(createResponse.adminId(), null)
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_STATUS);
    }

    /*
    사용자 상세 정보 조회
     */

    @Test
    @DisplayName("관리자는 사용자 상세 정보를 조회할 수 있다")
    void getMember_success_byAdmin() throws Exception {
        // given
        CustomUserPrincipal member = loginAsActiveMaleMember();

        // when
        AdminMemberResponse response = adminService.getMember(member.getId());

        // then
        assertAll(
            () -> assertThat(response.memberId()).isEqualTo(member.getId()),
            () -> assertThat(response.status()).isEqualTo(MemberStatus.ACTIVE)
        );
    }

    @Test
    @DisplayName("관리자는 사용자 상태를 변경할 수 있다")
    void updateMemberStatus_success_byAdmin() {
        // given
        CustomUserPrincipal member = loginAsActiveMaleMember();

        AdminMemberStatusUpdateRequest request =
            new AdminMemberStatusUpdateRequest(MemberStatus.SUSPENDED);

        // when
        AdminMemberResponse response = adminService.updateMemberStatus(
            member.getId(),
            request
        );

        // then
        Member updatedMember = memberRepository.findById(member.getId())
            .orElseThrow();

        assertAll(
            () -> assertThat(response.memberId()).isEqualTo(member.getId()),
            () -> assertThat(response.status()).isEqualTo(MemberStatus.SUSPENDED),
            () -> assertThat(updatedMember.getStatus()).isEqualTo(MemberStatus.SUSPENDED)
        );
    }

    @Test
    @DisplayName("사용자 상세 조회 실패 - 사용자 없음")
    void getMember_fail_userNotFound() {
        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.getMember(null)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 상태 변경 실패 - 사용자 없음")
    void updateMemberStatus_fail_userNotFound() {
        AdminMemberStatusUpdateRequest request =
            new AdminMemberStatusUpdateRequest(MemberStatus.SUSPENDED);

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateMemberStatus(null, request)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 상태 변경 실패 - requset")
    void updateMemberStatus_fail_request() {
        CustomUserPrincipal member = loginAsActiveMaleMember();

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateMemberStatus(member.getId(), null)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.INVALID_STATUS);
    }

    /*
    판매자 상세 정보 조회
     */

    @Test
    @DisplayName("관리자는 판매자 상세 정보를 조회할 수 있다")
    void getSeller_success_byAdmin() {
        // given
        CustomUserPrincipal seller = loginAsPendingSeller();

        // when
        AdminSellerResponse response = adminService.getSeller(seller.getId());

        // then
        assertAll(
            () -> assertThat(response.sellerId()).isEqualTo(seller.getId()),
            () -> assertThat(response.sellerStatus()).isEqualTo(SellerStatus.PENDING)
        );
    }

    @Test
    @DisplayName("관리자는 판매자 상태를 APPROVED로 변경할 수 있다")
    void updateSellerStatus_success_approved() {
        // given
        CustomUserPrincipal seller = loginAsPendingSeller();

        AdminSellerStatusUpdateRequest request =
            new AdminSellerStatusUpdateRequest(SellerStatus.APPROVED);

        // when
        AdminSellerResponse response = adminService.updateSellerStatus(
            seller.getId(),
            request
        );

        // then
        Seller updatedSeller = sellerRepository.findById(seller.getId())
            .orElseThrow();

        assertAll(
            () -> assertThat(response.sellerId()).isEqualTo(seller.getId()),
            () -> assertThat(response.sellerStatus()).isEqualTo(SellerStatus.APPROVED),

            () -> assertThat(updatedSeller.getSellerStatus()).isEqualTo(SellerStatus.APPROVED),
            () -> assertThat(updatedSeller.getApprovedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("관리자는 판매자 상태를 BLOCKED로 변경할 수 있다")
    void updateSellerStatus_success_blocked() {
        // given
        CustomUserPrincipal seller = loginAsPendingSeller();

        AdminSellerStatusUpdateRequest request =
            new AdminSellerStatusUpdateRequest(SellerStatus.BLOCKED);

        // when
        AdminSellerResponse response = adminService.updateSellerStatus(
            seller.getId(),
            request
        );

        // then
        Seller updatedSeller = sellerRepository.findById(seller.getId())
            .orElseThrow();

        assertAll(
            () -> assertThat(response.sellerId()).isEqualTo(seller.getId()),
            () -> assertThat(response.sellerStatus()).isEqualTo(SellerStatus.BLOCKED),
            () -> assertThat(updatedSeller.getSellerStatus()).isEqualTo(SellerStatus.BLOCKED)
        );
    }

    @Test
    @DisplayName("판매자 상세 조회 실패 - 판매자 없음")
    void getSeller_fail_sellerNotFound() {
        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.getSeller(null)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.SELLER_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자 상태 변경 실패 - 판매자 없음")
    void updateSellerStatus_fail_sellerNotFound() {
        AdminSellerStatusUpdateRequest request =
            new AdminSellerStatusUpdateRequest(SellerStatus.APPROVED);

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateSellerStatus(null, request)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.SELLER_NOT_FOUND);
    }

    @Test
    @DisplayName("판매자 상태 변경 실패 - 상태값이 null")
    void updateSellerStatus_fail_nullStatus() {
        // given
        CustomUserPrincipal seller = loginAsPendingSeller();

        // when
        AppException exception = assertThrows(
            AppException.class,
            () -> adminService.updateSellerStatus(seller.getId(), null)
        );

        // then
        assertThat(exception.getErrorCode())
            .isEqualTo(MemberErrorCode.INVALID_STATUS);
    }

    private Long findAdminIdByEmail(String email) {
        String emailHash = emailHashEncoder.encode(email);

        return adminRepository.findByEmailHash(emailHash)
            .orElseThrow()
            .getId();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private Admin createAdmin(
        String email,
        String password,
        String name,
        AdminRole role,
        AdminStatus status
    ) {
        Admin admin = Admin.builder()
            .emailHash(emailHashEncoder.encode(email))
            .emailEncrypted(dataEncryptor.encrypt(email))
            .password(passwordEncoder.encode(password))
            .name(dataEncryptor.encrypt(name))
            .adminRole(role)
            .adminStatus(status)
            .build();

        return adminRepository.saveAndFlush(admin);
    }
}