package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.AttributeService;
import com.example.i_commerce.domain.product.controller.request.CreateAttributeRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attributes")
public class AttributeController {

    private final AttributeService attributeService;

    @PostMapping
    public ApiResponse<Void> createAttribute(
        @Valid @RequestBody CreateAttributeRequest request
    ) {
        attributeService.createAttribute(request);
        return ApiResponse.success();
    }

}
