package com.example.i_commerce.global.security.jwt;

import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain //다음 필터 또는 Controller로 요청을 넘기는 통로
    ) throws ServletException, IOException {//프레임워크에 처리 위임

        //프론트에서 헤더에 Authorization 넣어줘야 함
        String authorization = request.getHeader("Authorization");

        //Authorization헤더는 <인증방식> <인증정보> 구조이다.
        //Bearer 은 인증방식, 토큰은 인증정보

        //토큰 없으면 그냥 통과
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            TokenPayload payload = jwtTokenUtil.parseToken(token);

            CustomUserPrincipal principal =
                CustomUserPrincipal.fromTokenPayload(payload);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
                );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
