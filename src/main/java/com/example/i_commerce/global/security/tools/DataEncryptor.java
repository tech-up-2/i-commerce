package com.example.i_commerce.global.security.tools;

import org.springframework.stereotype.Component;

@Component
public interface DataEncryptor {

    byte[] encrypt(String text); // "암호화해줘"라는 행위만 정의

    String decrypt(byte[] cipherText);
}