package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.controller.request.CreateProductRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.ItemAttributeRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.OptionRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.OptionValueRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.ProductItemRequest;
import com.example.i_commerce.domain.product.enums.OptionType;
import java.util.List;

public class CreateProductRequestFixture {
    /**
     * 단일 옵션 상품
     */
    public static CreateProductRequest singleOptionRequest() {
        return CreateProductRequest.builder()
            .storeId(1L)
            .categoryId(1L)
            .name("아이폰 15 프로")
            .description("최신 아이폰입니다")
            .optionType(OptionType.SINGLE)
            .options(List.of(
                OptionRequest.builder()
                    .optionOrder(1)
                    .name("색상")
                    .values(List.of(
                        new OptionValueRequest("블랙", 1),
                        new OptionValueRequest("화이트", 2),
                        new OptionValueRequest("골드", 3)
                    ))
                    .build()
            ))
            .items(List.of(
                ProductItemRequest.builder()
                    .sku("IP15-BLK")
                    .price(1500000)
                    .displayName("아이폰 15 프로 - 블랙")
                    .optionValues(List.of("블랙"))
                    .stock(100)
                    .isDefault(true)
                    .attributes(List.of(
                        new ItemAttributeRequest(1L, "색상: 블랙", 1)
                    ))
                    .build(),
                ProductItemRequest.builder()
                    .sku("IP15-WHT")
                    .price(1500000)
                    .displayName("아이폰 15 프로 - 화이트")
                    .optionValues(List.of("화이트"))
                    .stock(50)
                    .isDefault(false)
                    .attributes(List.of(
                        new ItemAttributeRequest(1L, "색상: 화이트", 1)
                    ))
                    .build()
            ))
            .build();
    }

    /**
     * 다중 옵션 상품
     */
    public static CreateProductRequest doubleOptionRequest() {
        return CreateProductRequest.builder()
            .storeId(1L)
            .categoryId(1L)
            .name("갤럭시 S24")
            .description("삼성 플래그십")
            .optionType(OptionType.DOUBLE)
            .options(List.of(
                OptionRequest.builder()
                    .optionOrder(1)
                    .name("색상")
                    .values(List.of(
                        new OptionValueRequest("블랙", 1),
                        new OptionValueRequest("화이트", 2)
                    ))
                    .build(),
                OptionRequest.builder()
                    .optionOrder(2)
                    .name("용량")
                    .values(List.of(
                        new OptionValueRequest("256GB", 1),
                        new OptionValueRequest("512GB", 2)
                    ))
                    .build()
            ))
            .items(List.of(
                ProductItemRequest.builder()
                    .sku("S24-BLK-256")
                    .price(1200000)
                    .displayName("갤럭시 S24 - 블랙/256GB")
                    .optionValues(List.of("블랙", "256GB"))
                    .stock(100)
                    .isDefault(true)
                    .attributes(List.of(
                        new ItemAttributeRequest(1L, "색상: 블랙", 1),
                        new ItemAttributeRequest(3L, "용량: 256GB", 2)
                    ))
                    .build(),
                ProductItemRequest.builder()
                    .sku("S24-WHT-512")
                    .price(1400000)
                    .displayName("갤럭시 S24 - 화이트/512GB")
                    .optionValues(List.of("화이트", "512GB"))
                    .stock(50)
                    .isDefault(false)
                    .attributes(List.of(
                        new ItemAttributeRequest(1L, "색상: 화이트", 1),
                        new ItemAttributeRequest(4L, "용량: 512GB", 2)
                    ))
                    .build()
            ))
            .build();
    }


    /**
     * 옵션 없는 상품
     */
    public static CreateProductRequest noneOptionRequest() {
        return CreateProductRequest.builder()
            .storeId(1L)
            .categoryId(1L)
            .name("맥북 프로")
            .description("애플 노트북")
            .optionType(OptionType.NONE)
            .options(List.of())
            .items(List.of(
                ProductItemRequest.builder()
                    .sku("MBP-2024")
                    .price(2500000)
                    .displayName("맥북 프로 16인치")
                    .optionValues(List.of())
                    .stock(30)
                    .isDefault(true)
                    .attributes(List.of(
                            new ItemAttributeRequest(1L, "색상: 화이트", 1)
                    ))
                    .build()
            ))
            .build();
    }


}
