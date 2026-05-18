package com.example.i_commerce.global.s3.controller;

import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.s3.service.S3ImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "GlobalImageController", description = "이미  지 업로드 및 삭제 테스트 용 API")
@RequiredArgsConstructor
public class GlobalImageController {

    private final S3ImageService s3ImageService;


    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileUrl = s3ImageService.uploadImage(file, "review");
        return ApiResponse.success(fileUrl); // 성공 시 S3 URL 문자열 반환
    }

    @PreAuthorize("hasRole('MEMBER')")
    @DeleteMapping("/delete")
    public ApiResponse<String> deleteImage(@RequestParam("fileUrl") String fileUrl) {
        s3ImageService.deleteImage(fileUrl);
        return ApiResponse.success("S3 파일 삭제 완료");
    }

}
