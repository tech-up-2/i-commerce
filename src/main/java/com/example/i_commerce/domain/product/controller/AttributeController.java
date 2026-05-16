package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.AttributeService;
import com.example.i_commerce.domain.product.controller.request.CreateAttributeRequest;
import com.example.i_commerce.domain.product.controller.response.AttributeGroupResponse;
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

    @GetMapping
    public ApiResponse<List<AttributeGroupResponse>> getAllAttributes() {
        List<AttributeGroupResponse> res =
            attributeService.getAllAttributesGroupedByKey();
        return ApiResponse.success(res);
    }

}
