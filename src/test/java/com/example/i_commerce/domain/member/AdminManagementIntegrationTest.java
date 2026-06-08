//package com.example.i_commerce.domain.member;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.hamcrest.Matchers.hasItem;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import com.example.i_commerce.domain.member.entity.Admin;
//import com.example.i_commerce.domain.member.entity.enums.AdminRole;
//import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
//import com.example.i_commerce.domain.member.repository.AdminRepository;
//import com.example.i_commerce.domain.member.tools.DataEncryptor;
//import com.example.i_commerce.domain.member.tools.EmailHashEncoder;
//import com.example.i_commerce.domain.testtools.IntegrationTestSupport;
//import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//@TestPropertySource(properties = {
//    "app.bootstrap.admin.enabled=${ADMIN_ENABLED:true}",
//    "app.bootstrap.admin.email=${ADMIN_EMAIL:master@test.com}",
//    "app.bootstrap.admin.password=${ADMIN_PASSWORD:master123!}",
//    "app.bootstrap.admin.name=최초관리자"
//}, locations = "file:.env")
//class AdminManagementIntegrationTest extends IntegrationTestSupport {
//
//    @Autowired
//    MockMvc mockMvc;
//
//    ObjectMapper objectMapper = new ObjectMapper();
//
//    @Autowired
//    AdminRepository adminRepository;
//
//    @Autowired
//    EmailHashEncoder emailHashEncoder;
//
//    @Autowired
//    DataEncryptor dataEncryptor;
//
//    @Autowired
//    PasswordEncoder passwordEncoder;
//
//    @Test
//    @DisplayName("서버 시작 시 최초 MASTER 관리자가 자동 생성된다")
//    void bootstrapMasterAdmin_success() {
//        String masterEmailHash = emailHashEncoder.encode("master@test.com");
//
//        Admin master = adminRepository.findByEmailHash(masterEmailHash)
//            .orElseThrow();
//
//        assertThat(master.getAdminRole()).isEqualTo(AdminRole.MASTER);
//        assertThat(master.getAdminStatus()).isEqualTo(AdminStatus.ACTIVE);
//        assertThat(passwordEncoder.matches("master123!", master.getPassword())).isTrue();
//
//        String email = dataEncryptor.decrypt(master.getEmailEncrypted());
//        String name = dataEncryptor.decrypt(master.getName());
//
//        assertThat(email).isEqualTo("master@test.com");
//        assertThat(name).isEqualTo("최초관리자");
//    }
//
//    @Test
//    @DisplayName("MASTER 관리자는 로그인 후 새 관리자를 생성할 수 있다")
//    void createAdmin_success_byMaster() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        mockMvc.perform(post("/api/v1/admin/manage")
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "email": "admin1@test.com",
//                      "password": "admin123!",
//                      "name": "관리자1",
//                      "adminRole": "ADMIN"
//                    }
//                    """))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andExpect(jsonPath("$.data.email").value("admin1@test.com"))
//            .andExpect(jsonPath("$.data.name").value("관리자1"))
//            .andExpect(jsonPath("$.data.adminRole").value("ADMIN"))
//            .andExpect(jsonPath("$.data.adminStatus").value("ACTIVE"));
//    }
//
//    @Test
//    @DisplayName("MASTER 관리자는 관리자 목록을 조회할 수 있다")
//    void getAdmins_success_byMaster() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        createAdminByApi(token, "admin-list@test.com", "admin123!", "목록관리자", "ADMIN");
//
//        mockMvc.perform(get("/api/v1/admin/manage")
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .param("page", "0")
//                .param("size", "20"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andExpect(jsonPath("$.data.content[*].email", hasItem("master@test.com")))
//            .andExpect(jsonPath("$.data.content[*].email", hasItem("admin-list@test.com")));
//    }
//
//    @Test
//    @DisplayName("MASTER 관리자는 다른 관리자의 권한을 변경할 수 있다")
//    void updateAdminRole_success_byMaster() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        Long adminId = createAdminByApi(
//            token,
//            "adminRole-target@test.com",
//            "admin123!",
//            "권한변경대상",
//            "ADMIN"
//        );
//
//        mockMvc.perform(patch("/api/v1/admin/manage/{adminId}/role", adminId)
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "adminRole": "OPERATOR"
//                    }
//                    """))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andExpect(jsonPath("$.data.adminId").value(adminId))
//            .andExpect(jsonPath("$.data.adminRole").value("OPERATOR"))
//            .andExpect(jsonPath("$.data.adminStatus").value("ACTIVE"));
//    }
//
//    @Test
//    @DisplayName("MASTER 관리자는 다른 관리자의 상태를 변경할 수 있다")
//    void updateAdminStatus_success_byMaster() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        Long adminId = createAdminByApi(
//            token,
//            "status-target@test.com",
//            "admin123!",
//            "상태변경대상",
//            "ADMIN"
//        );
//
//        mockMvc.perform(patch("/api/v1/admin/manage/{adminId}/status", adminId)
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "adminStatus": "SUSPENDED"
//                    }
//                    """))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andExpect(jsonPath("$.data.adminId").value(adminId))
//            .andExpect(jsonPath("$.data.adminRole").value("ADMIN"))
//            .andExpect(jsonPath("$.data.adminStatus").value("SUSPENDED"));
//    }
//
//    @Test
//    @DisplayName("마지막 ACTIVE MASTER 관리자는 ADMIN으로 권한 변경할 수 없다")
//    void updateLastActiveMasterRole_fail() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        Long masterId = findAdminIdByEmail("master@test.com");
//
//        mockMvc.perform(patch("/api/v1/admin/manage/{adminId}/role", masterId)
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "adminRole": "ADMIN"
//                    }
//                    """))
//            .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("마지막 ACTIVE MASTER 관리자는 SUSPENDED 상태로 변경할 수 없다")
//    void updateLastActiveMasterStatus_fail() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        Long masterId = findAdminIdByEmail("master@test.com");
//
//        mockMvc.perform(patch("/api/v1/admin/manage/{adminId}/status", masterId)
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "adminStatus": "SUSPENDED"
//                    }
//                    """))
//            .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("ACTIVE MASTER가 2명 이상이면 기존 MASTER를 ADMIN으로 변경할 수 있다")
//    void updateMasterRole_success_whenAnotherActiveMasterExists() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        Long secondMasterId = createAdminByApi(
//            token,
//            "master2@test.com",
//            "master123!",
//            "두번째마스터",
//            "MASTER"
//        );
//
//        Long firstMasterId = findAdminIdByEmail("master@test.com");
//
//        mockMvc.perform(patch("/api/v1/admin/manage/{adminId}/role", firstMasterId)
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "adminRole": "ADMIN"
//                    }
//                    """))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.data.adminId").value(firstMasterId))
//            .andExpect(jsonPath("$.data.adminRole").value("ADMIN"));
//
//        Admin secondMaster = adminRepository.findById(secondMasterId).orElseThrow();
//        assertThat(secondMaster.getAdminRole()).isEqualTo(AdminRole.MASTER);
//        assertThat(secondMaster.getAdminStatus()).isEqualTo(AdminStatus.ACTIVE);
//    }
//
//    @Test
//    @DisplayName("관리자는 사용자 상세 정보를 조회할 수 있다")
//    void getMember_success_byAdmin() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        CustomUserPrincipal member = loginAsActiveMaleMember();
//
//        mockMvc.perform(get("/api/v1/admin/manage/users/{userId}", member.getId())
//                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andExpect(jsonPath("$.data.memberId").value(member.getId()))
//            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
//    }
//
//    @Test
//    @DisplayName("관리자는 사용자 상태를 변경할 수 있다")
//    void updateMemberStatus_success_byAdmin() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        CustomUserPrincipal member = loginAsActiveMaleMember();
//
//        mockMvc.perform(patch("/api/v1/admin/manage/users/{userId}/status", member.getId())
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "memberStatus": "SUSPENDED"
//                    }
//                    """))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andExpect(jsonPath("$.data.memberId").value(member.getId()))
//            .andExpect(jsonPath("$.data.status").value("SUSPENDED"));
//    }
//
//    @Test
//    @DisplayName("관리자는 판매자 상세 정보를 조회할 수 있다")
//    void getSeller_success_byAdmin() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        CustomUserPrincipal seller = loginAsPendingSeller();
//
//        mockMvc.perform(get("/api/v1/admin/manage/sellers/{sellerId}", seller.getId())
//                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andExpect(jsonPath("$.data.sellerId").value(seller.getId()))
//            .andExpect(jsonPath("$.data.sellerStatus").value("PENDING"));
//    }
//
//    @Test
//    @DisplayName("관리자는 판매자 상태를 변경할 수 있다")
//    void updateSellerStatus_success_byAdmin() throws Exception {
//        String token = loginAndGetToken("master@test.com", "master123!");
//
//        CustomUserPrincipal seller = loginAsPendingSeller();
//
//        mockMvc.perform(patch("/api/v1/admin/manage/sellers/{sellerId}/status", seller.getId())
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "sellerStatus": "APPROVED"
//                    }
//                    """))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andExpect(jsonPath("$.data.sellerId").value(seller.getId()))
//            .andExpect(jsonPath("$.data.sellerStatus").value("APPROVED"));
//    }
//
//    private String loginAndGetToken(String email, String password) throws Exception {
//        String responseBody = mockMvc.perform(post("/api/v1/admin/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "email": "%s",
//                      "password": "%s"
//                    }
//                    """.formatted(email, password)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andExpect(jsonPath("$.data.accessToken").exists())
//            .andReturn()
//            .getResponse()
//            .getContentAsString();
//
//        JsonNode root = objectMapper.readTree(responseBody);
//        return root.path("data").path("accessToken").asText();
//    }
//
//    private Long createAdminByApi(
//        String token,
//        String email,
//        String password,
//        String name,
//        String role
//    ) throws Exception {
//        String responseBody = mockMvc.perform(post("/api/v1/admin/manage")
//                .header(HttpHeaders.AUTHORIZATION, bearer(token))
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "email": "%s",
//                      "password": "%s",
//                      "name": "%s",
//                      "adminRole": "%s"
//                    }
//                    """.formatted(email, password, name, role)))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$.code").value("SUCCESS"))
//            .andReturn()
//            .getResponse()
//            .getContentAsString();
//
//        JsonNode root = objectMapper.readTree(responseBody);
//        return root.path("data").path("adminId").asLong();
//    }
//
//    private Long findAdminIdByEmail(String email) {
//        String emailHash = emailHashEncoder.encode(email);
//
//        return adminRepository.findByEmailHash(emailHash)
//            .orElseThrow()
//            .getId();
//    }
//
//    private String bearer(String token) {
//        return "Bearer " + token;
//    }
//}