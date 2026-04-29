package com.example.i_commerce.global.security.checker;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authChecker")
public class AuthChecker {

    // =========================
    // Member
    // =========================

    // 정보조회
    public boolean canViewMemberInfo() {
        return isMember()
            && hasAny("STATUS_ACTIVE", "STATUS_SUSPENDED");
    }

    // 장바구니
    public boolean canUseCart() {
        return isMember()
            && hasAny("STATUS_ACTIVE", "STATUS_SUSPENDED");
    }

    // 주문 / 결제
    public boolean canOrderAndPay() {
        return isMember()
            && has("STATUS_ACTIVE");
    }

    // 리뷰 작성
    public boolean canWriteReviewAsMember() {
        return isMember()
            && has("STATUS_ACTIVE");
    }

    // 탈퇴 요청
    public boolean canWithdrawMember() {
        return isMember()
            && hasAny("STATUS_ACTIVE", "STATUS_SUSPENDED");
    }

    // =========================
    // Seller
    // =========================

    // 판매자 정보 조회
    public boolean canViewSellerInfo() {
        return isSeller()
            && hasAny("STATUS_ACTIVE", "STATUS_SUSPENDED")
            && hasAny("SELLER_PENDING", "SELLER_APPROVED", "SELLER_BLOCKED");
    }

    // 상점 조회
    public boolean canViewStore() {
        return isSeller()
            && has("STATUS_ACTIVE")
            && has("SELLER_APPROVED");
    }

    // 상품 관리
    public boolean canManageSellerProduct() {
        return isSeller()
            && has("STATUS_ACTIVE")
            && has("SELLER_APPROVED");
    }

    // 주문 관리
    public boolean canManageSellerOrder() {
        return isSeller()
            && has("STATUS_ACTIVE")
            && has("SELLER_APPROVED");
    }

    // 리뷰 관리
    public boolean canManageSellerReview() {
        return isSeller()
            && has("STATUS_ACTIVE")
            && has("SELLER_APPROVED");
    }

    // 판매자 탈퇴 요청
    public boolean canWithdrawSeller() {
        return isSeller()
            && hasAny("STATUS_ACTIVE", "STATUS_SUSPENDED")
            && hasAny("SELLER_PENDING", "SELLER_APPROVED", "SELLER_BLOCKED");
    }

    // =========================
    // Admin
    // =========================

    // 관리자 관리
    public boolean canManageAdmin() {
        return isAdmin()
            && has("STATUS_ACTIVE")
            && has("ADMIN_MASTER");
    }

    // 사용자, 판매자 관리
    public boolean canManageUserAndSeller() {
        return isAdmin()
            && has("STATUS_ACTIVE")
            && hasAny("ADMIN_MASTER", "ADMIN_ADMIN", "ADMIN_OPERATOR");
    }

    // 카테고리 관리
    public boolean canManageCategory() {
        return isAdmin()
            && has("STATUS_ACTIVE")
            && hasAny("ADMIN_MASTER", "ADMIN_ADMIN");
    }

    // 시스템 옵션
    public boolean canManageSystemOption() {
        return isAdmin()
            && has("STATUS_ACTIVE")
            && hasAny("ADMIN_MASTER", "ADMIN_ADMIN");
    }

    // 리뷰 관리
    public boolean canManageReviewAsAdmin() {
        return isAdmin()
            && has("STATUS_ACTIVE")
            && hasAny("ADMIN_MASTER", "ADMIN_ADMIN", "ADMIN_OPERATOR");
    }

    // 채팅 관리
    public boolean canManageChatAsAdmin() {
        return isAdmin()
            && has("STATUS_ACTIVE")
            && hasAny("ADMIN_MASTER", "ADMIN_ADMIN", "ADMIN_OPERATOR");
    }

    // =========================
    // 공통 기능
    // =========================

    // 주문 취소: member / seller / admin 모두 가능
    public boolean canCancelOrder() {
        return canOrderAndPay()
            || canManageSellerOrder()
            || canManageUserAndSeller();
    }

    //문의 작성: member / seller
    public boolean canWriteInquiry() {
        return isSeller()
            && hasAny("STATUS_ACTIVE", "STATUS_SUSPENDED");
    }

    // 리뷰 삭제: 작성자(member) 또는 관리자
    public boolean canDeleteReview() {
        return canWriteReviewAsMember()
            || canManageReviewAsAdmin();
    }

    // 좋아요: member, seller
    public boolean canLike() {
        return isMember()
            && has("STATUS_ACTIVE");
    }

    // 리뷰 신고: member, seller
    public boolean canReportReview() {
        return isMember()
            && has("STATUS_ACTIVE");
    }

    // 채팅: member, seller
    public boolean chat() {
        return isMember()
            && has("STATUS_ACTIVE");
    }


    //마스터 권한
    public boolean master() {
        return isAdmin() && has("ADMIN_MASTER");
    }


    // =========================
    // 내부 공통 메서드
    // =========================

    private boolean isMember() {
        return has("ROLE_MEMBER");
    }

    private boolean isSeller() {
        return has("ROLE_MEMBER")
            && has("ROLE_SELLER");
    }

    private boolean isAdmin() {
        return has("ROLE_ADMIN");
    }

    private boolean has(String authority) {
        return getAuthorities().contains(authority);
    }

    private boolean hasAny(String... authorities) {
        Set<String> currentAuthorities = getAuthorities();

        for (String authority : authorities) {
            if (currentAuthorities.contains(authority)) {
                return true;
            }
        }

        return false;
    }

    private Set<String> getAuthorities() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }

        return authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }
}