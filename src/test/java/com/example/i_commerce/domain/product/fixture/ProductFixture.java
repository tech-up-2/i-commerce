package com.example.i_commerce.domain.product.fixture;


import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductFixture {

    public static Product.ProductBuilder defaultProduct() {
        return Product
            .builder()
            .storeId(1L)
            .category(CategoryFixture.rootCategory())
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .optionType(ProductOptionType.NONE)
            .status(ProductStatus.ON_SALE)
            .images(List.of());
    }

    public static Product createNoneOptionProduct(ProductItem defaultItem) {
        return defaultProduct()
            .items(List.of(defaultItem))
            .optionType(ProductOptionType.NONE)
            .build();
    }

    public static Product singleOptionProduct() {
        ProductOptionValue optionValue =
            ProductOptionValue.of(1, "색상", "빨강", 1);
        ProductItem defaultItem = ProductItemFixture.createSingleOptionItem(optionValue);

        return defaultProduct()
            .optionType(ProductOptionType.SINGLE)
            .options(List.of(optionValue))
            .items(List.of(defaultItem))
            .build();
    }

    public static Product createSingleOptionProduct() {
        ProductOptionValue optionValue = ProductOptionValue
            .of(1, "색상", "빨강", 1);
        ProductItem defaultItem = ProductItemFixture.createSingleOptionItem(optionValue);
        return defaultProduct()
            .optionType(ProductOptionType.SINGLE)
            .options(List.of(optionValue))
            .items(List.of(defaultItem))
            .build();
    }

    public static Product createDoubleOptionProduct() {
        ProductOptionValue optionValue1 = ProductOptionValue
            .of(1, "색상", "빨강", 1);
        ProductOptionValue optionValue2 = ProductOptionValue
            .of(2, "크기", "S", 2);

        ProductItem defaultItem = ProductItemFixture.createDoubleOptionItem(
            optionValue1, optionValue2
        );

        return defaultProduct()
            .optionType(ProductOptionType.DOUBLE)
            .items(List.of(defaultItem))
            .options(List.of(optionValue1, optionValue2))
            .build();
    }

    public static Product createProduct(Long id, Long storeId, ProductOptionType optionType) {
        Product product = Product.builder()
            .storeId(storeId)
            .category(CategoryFixture.createRootWithId(1L))
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .optionType(optionType)
            .status(ProductStatus.ON_SALE)
            .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    public static CreateProductRequest createProductRequest(
        Long storeId,
        Long categoryId,
        ProductOptionType optionType
    ) {
        return CreateProductRequest.builder()
            .storeId(storeId)
            .name("테스트 상품")
            .description("테스트 상품 설명")
            .categoryId(categoryId)
            .productOptionType(optionType)
            .mainImageUrl("http://image.com/main.jpg")
            .imageUrls(List.of())
            .options(List.of())
            .items(List.of(createProductItemRequest()))
            .build();
    }

    public static CreateProductRequest.ProductItemRequest createProductItemRequest() {
        return CreateProductRequest.ProductItemRequest.builder()
            .sku("SKU-001")
            .price(10000)
            .stock(100)
            .isDefault(true)
            .build();
    }

}
