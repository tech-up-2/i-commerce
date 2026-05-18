package com.example.i_commerce.domain.product.controller;


import com.example.i_commerce.domain.product.application.service.AttributeService;
import com.example.i_commerce.domain.product.controller.request.CreateAttributeRequest;
import com.example.i_commerce.domain.product.controller.response.AttributeGroupResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Attribute API", description = "속성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attributes")
public class AttributeController {

    private final AttributeService attributeService;

    @Operation(summary = "속성 생성", description = "제공할 속성을 생성합니다.")
    @PostMapping
    public ApiResponse<Void> createAttribute(
        @Valid @RequestBody CreateAttributeRequest request
    ) {
        attributeService.createAttribute(request);
        return ApiResponse.success();
    }

    @Operation(summary = "전체 속성 조회", description = "존재하는 모든 속성을 조회합니다.")
    @GetMapping
    public ApiResponse<List<AttributeGroupResponse>> getAllAttributes() {
        List<AttributeGroupResponse> res =
            attributeService.getAllAttributesGroupedByKey();
        return ApiResponse.success(res);
    }

}
