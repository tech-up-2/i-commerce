package com.example.i_commerce.global.s3.service;

import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.exception.common.CommonErrorCode;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadImage(MultipartFile file, String folder) {
        if(file.isEmpty()) {
            throw new AppException(CommonErrorCode.INVALID_MULTIPART_FILE);
        }

        String originalFilename = file.getOriginalFilename();
        String s3FileName = folder + "/" + generateS3FileName(originalFilename);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3FileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(s3FileName)).toExternalForm();

        } catch (IOException | SdkException e) {
            e.printStackTrace();
            throw new AppException(CommonErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public void deleteImage(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        String s3FileName = extractFileNameFromUrl(fileUrl);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(s3FileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    private String generateS3FileName(String originalFilename) {
        int dotIndex = (originalFilename != null) ? originalFilename.lastIndexOf(".") : -1;
        String ext = (dotIndex != -1) ? originalFilename.substring(dotIndex) : "";
        return UUID.randomUUID() + ext;
    }

    private String extractFileNameFromUrl(String fileUrl) {

        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();

            String decodedUri = URLDecoder.decode(path, StandardCharsets.UTF_8);

            if(decodedUri.startsWith("/")) {
                decodedUri = decodedUri.substring(1);
            }

            return decodedUri;

        } catch (Exception e) {
            throw new AppException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

}
