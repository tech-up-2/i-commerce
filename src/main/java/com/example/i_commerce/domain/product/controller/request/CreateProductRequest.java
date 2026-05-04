package com.example.i_commerce.domain.product.controller.request;


import com.example.i_commerce.domain.product.entity.OptionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Builder
public record CreateProductRequest(
    @NotNull
    Long storeId,

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
    String name,

    @Size(max = 1000, message = "상품 설명은 1000자 이하여야 합니다.")
    String description,

    @NotNull(message = "카테고리는 필수입니다.")
    Long categoryId,

    @NotNull(message = "옵션 타입은 필수입니다.")
    OptionType optionType,

    @NotNull(message = "대표 이미지 URL은 필수입니다.")
    String mainImageUrl,

    List<String> imageUrls,

    List<OptionRequest> options,

    @NotEmpty(message = "상품 아이템은 1개 이상 필요합니다.")
    List<ProductItemRequest> items


) {

    public CreateProductRequest {
        imageUrls = imageUrls != null ? imageUrls : List.of();
        options = options != null ? options : List.of();
    }

    @Builder
    public record OptionRequest(
        @NotNull
        @Min(value = 1) @Max(value = 2)
        Integer optionOrder,

        @NotNull(message = "옵션 ID는 필수입니다.")
        Long optionId,

        @NotBlank(message = "옵션 이름은 필수입니다.")
        @Size(max = 50)
        String name,

        @NotEmpty(message = "옵션 값은 1개 이상 필요합니다.")
        List<OptionValueRequest> values

    ) {}

    @Builder
    public record OptionValueRequest(
        @NotBlank(message = "옵션 값은 필수입니다.")
        String value,

        @NotNull
        @Min(0)
        Integer displayOrder

    ) {}

    @Builder
    public record ProductItemRequest(

        List<String> optionValues,

        String displayName,

        @NotNull
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @NotNull
        @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        Integer stock,

        @NotBlank(message = "SKU는 필수입니다.")
        @Size(max = 100)
        String sku,

        List<ItemAttributeRequest> attributes,

        Boolean isDefault

    ) {
        public ProductItemRequest {
            isDefault = isDefault != null && isDefault;
        }
    }

    public record ItemAttributeRequest(
        @NotNull
        Long attributeId,
        String displayName,
        Integer displayOrder

    ) {}

}
