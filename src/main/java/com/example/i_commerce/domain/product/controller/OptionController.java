package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.OptionService;
import com.example.i_commerce.domain.product.controller.request.CreateOptionRequest;
import com.example.i_commerce.domain.product.controller.response.OptionGroupResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/options")
public class OptionController {

    private final OptionService optionService;

    @PostMapping
    public ApiResponse<Void> createOption(
        @Valid @RequestBody CreateOptionRequest request
    ) {
        optionService.createOption(request);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<List<OptionGroupResponse>> getAllOptions() {
        List<OptionGroupResponse> res = optionService.getAllOptionsGroupedByType();
        return ApiResponse.success(res);
    }

}
