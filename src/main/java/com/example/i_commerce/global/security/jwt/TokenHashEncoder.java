package com.example.i_commerce.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenHashEncoder {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKeySpec secretKeySpec;

    public TokenHashEncoder(
        @Value("${security.token-hmac-key}") String hmacKey
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(hmacKey);
        this.secretKeySpec = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
    }

    public String encode(String token) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] result = mac.doFinal(
                token.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException("토큰 해시 생성 실패", e);
        }
    }
}
