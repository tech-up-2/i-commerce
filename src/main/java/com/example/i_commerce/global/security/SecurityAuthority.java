package com.example.i_commerce.global.security;

public final class SecurityAuthority {
    private SecurityAuthority() {
    }

    public static final String ROLE_MEMBER = "ROLE_MEMBER";
    public static final String ROLE_SELLER = "ROLE_SELLER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String STATUS_ACTIVE = "STATUS_ACTIVE";
    public static final String STATUS_INACTIVE = "STATUS_INACTIVE";
    public static final String STATUS_SUSPENDED = "STATUS_SUSPENDED";
    public static final String STATUS_WITHDRAWN = "STATUS_WITHDRAWN";

    public static final String ADMIN_MASTER = "ADMIN_MASTER";
    public static final String ADMIN_ADMIN = "ADMIN_ADMIN";
    public static final String ADMIN_OPERATOR = "ADMIN_OPERATOR";

    public static final String SELLER_APPROVED = "SELLER_APPROVED";
    public static final String SELLER_PENDING = "SELLER_PENDING";
    public static final String SELLER_SUSPENDED = "SELLER_SUSPENDED";
}
