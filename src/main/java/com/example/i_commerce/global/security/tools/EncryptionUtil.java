package com.example.i_commerce.global.security.tools;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil implements DataEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128; // 인증 태그 길이 (128비트)
    private static final int IV_LENGTH_BYTE = 12;  // GCM 권장 IV 길이 (12바이트)
    private final SecretKey key;

    // 생성자에서 키를 불러오는 로직
    public EncryptionUtil(@Value("${app.encryption.key}") String rawKey) {
        byte[] keyBytes = Base64.getDecoder().decode(rawKey);
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public byte[] encrypt(String plainText) {
        try {
            // 1. 랜덤 IV 생성 (12바이트)
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            // 2. Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // 3. 암호화 수행 (결과물에 자동으로 태그가 붙음)
            byte[] cipherText = cipher.doFinal(plainText.getBytes());

            // 4. [IV] + [암호문+태그] 합치기
            // VARBINARY(256)에 담기 위해 하나의 바이트 배열로 결합
            return ByteBuffer.allocate(iv.length + cipherText.length)
                .put(iv)
                .put(cipherText)
                .array();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decrypt(byte[] combined) {
        try {
            // 1. 합쳐진 배열에서 IV와 암호문 분리
            ByteBuffer buffer = ByteBuffer.wrap(combined);

            byte[] iv = new byte[IV_LENGTH_BYTE];
            buffer.get(iv); // 앞의 12바이트 읽기

            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText); // 나머지 읽기

            // 2. Cipher 초기화 (복호화 모드)
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            // 3. 복호화 수행 (태그가 변조되었다면 여기서 Exception 발생)
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
