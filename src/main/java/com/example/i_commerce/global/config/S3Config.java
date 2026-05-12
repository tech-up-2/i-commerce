package com.example.i_commerce.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

@Slf4j
@Configuration
public class S3Config {
    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Bean
    @Profile("dev")
    public AwsCredentialsProvider awsCredentialsProvider(
            @Value("${spring.cloud.aws.credentials.access-key}") String accessKey,
            @Value("${spring.cloud.aws.credentials.secret-key}") String secretKey) {

        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }
    // 1. S3Client Bean 등록
    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public CommandLineRunner testS3Connection(S3Client s3Client) {
        return args -> {
            try {
                log.info("=== S3 연결 테스트 시작 (리전: {}) ===", region);

                // 모든 버킷 목록 조회 테스트
                ListBucketsResponse response = s3Client.listBuckets();
                log.info("✅ S3 연결 성공! 계정 내 버킷 개수: {}", response.buckets().size());

                // 내가 사용할 버킷이 존재하는지 확인
                boolean bucketExists = response.buckets().stream()
                        .anyMatch(b -> b.name().contains(bucketName));

                if (bucketExists) {
                    log.info("✅ 설정된 버킷 '{}'을 정상적으로 찾았습니다.", bucketName);
                } else {
                    log.warn("⚠️ 연결은 성공했으나, 설정된 버킷 이름 '{}'을 찾을 수 없습니다. (버킷 이름을 확인하세요)", bucketName);
                }

            } catch (Exception e) {
                log.error("❌ S3 연결 실패! 에러 메시지: {}", e.getMessage());
                log.error("팁: 로컬이라면 access-key 설정을, EC2라면 IAM 역할을 확인하세요.");
            }
        };
    }


}