package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.OptionService;
import com.example.i_commerce.domain.product.controller.request.CreateOptionRequest;
import com.example.i_commerce.domain.product.controller.response.OptionResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Option API", description = "옵션 API")
@RestController
@RequiredArgsConstructor
@PreAuthorize("@authChecker.canManageOption()")
@RequestMapping("/api/v1/options")
public class OptionController {

    private final OptionService optionService;

    @Operation(summary = "옵션 생성", description = "제공할 옵션을 생성합니다.")
    @PostMapping
    public ApiResponse<Void> createOption(
        @Valid @RequestBody CreateOptionRequest request
    ) {
        optionService.createOption(request);
        return ApiResponse.success();
    }

    @Operation(summary = "전체 옵션 조회", description = "존재하는 모든 옵션을 조회합니다.")
    @GetMapping
    public ApiResponse<List<OptionResponse>> getAllOptions() {
        return ApiResponse.success(optionService.getAllOptions());
    }

    @Operation(summary = "옵션 삭제", description = "옵션을 삭제합니다.")
    @DeleteMapping("/{optionId}")
    public ApiResponse<Void> deleteOption(@PathVariable Long optionId) {
        optionService.deleteOption(optionId);
        return ApiResponse.success();
    }

}
