package com.example.i_commerce.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final String secretKey;
    private final int expiration;
    private Key SECRET_KEY;

    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey, @Value("${jwt.expiration}") int expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        this.SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey),
            SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(String email, String role){
        Claims claims = Jwts.claims().setSubject(email); //claims 는 페이로드라고 생각하면 된다.
        claims.put("role",role);
        Date now = new Date();//현재시간
        String token = Jwts.builder()
            .setClaims(claims)//클레임값
            .setIssuedAt(now)//발행시간
            .setExpiration(new Date(now.getTime()+expiration*60*1000L))//만료일자 밀리초 단위로
            .signWith(SECRET_KEY)//암호값을 가지고 서명한다.
            .compact();
            return token;
    }
}
