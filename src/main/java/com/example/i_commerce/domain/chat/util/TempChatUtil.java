package com.example.i_commerce.domain.chat.util;

import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class TempChatUtil {
    public static Long getCurrentUserId() {
        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
        return principal.getId();
    }
}
