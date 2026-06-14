package com.example.i_commerce.domain.product.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.i_commerce.common.ProductIntegrationTestSupport;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.Seller;
import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.product.application.service.ProductService;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import com.example.i_commerce.domain.product.entity.enums.OptionInputType;
import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.AttributeFixture;
import com.example.i_commerce.domain.product.fixture.CategoryFixture;
import com.example.i_commerce.domain.product.fixture.OptionFixture;
import com.example.i_commerce.domain.product.fixture.ProductStoreFixture;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ItemAttributeRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.OptionRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.OptionValueRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ProductItemRequest;
import com.example.i_commerce.domain.product.presentation.response.CreatedProductResponse;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;


@DisplayName("상품 통합 테스트")
public class ProductIntegrationTest extends ProductIntegrationTestSupport {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductStoreFixture testFixture;

    private Member member;

    private Store store;

    private Category category;

    private Option colorOption;

    private Option sizeOption;

    private Attribute testAttribute;

    @BeforeEach
    void setUp() {
        member = testFixture.createMember();
        Seller seller = testFixture.createSeller(member);
        store = testFixture.createStore(seller.getId());

        Category rootCategory = CategoryFixture.rootCategory();
        category = CategoryFixture.createChild(rootCategory, "상의");
        categoryRepository.saveAll(List.of(rootCategory, category));

        colorOption = OptionFixture.createOption("색상", OptionInputType.SELECT);
        sizeOption = OptionFixture.createOption("사이즈", OptionInputType.SELECT);
        optionRepository.saveAll(List.of(colorOption, sizeOption));

        testAttribute = AttributeFixture.createAttribute("소재", "면100%");
        attributeRepository.save(testAttribute);

        mapCategoryOption(category, sizeOption);
        mapCategoryOption(category, colorOption);
        mapCategoryAttribute(category, testAttribute);
    }

    private CreateProductRequest.CreateProductRequestBuilder basicProductRequest() {
        return CreateProductRequest.builder()
            .storeId(store.getId())
            .categoryId(category.getId())
            .productOptionType(ProductOptionType.NONE)
            .name("티셔츠")
            .mainImageUrl("https://cdn.example.com/image/main.jpg");
    }

    private ProductItemRequest buildSingleOptionItem(
        String sku, String colorValue, int stock, boolean isDefault
    ) {
        return ProductItemRequest.builder()
            .sku(sku)
            .price(29900)
            .stock(stock)
            .optionValues(List.of(colorValue))
            .displayName(colorValue)
            .isDefault(isDefault)
            .build();
    }

    private ProductItemRequest buildDoubleOptionItem(
        String sku, String colorValue, String sizeValue, int stock, boolean isDefault
    ) {
        return ProductItemRequest.builder()
            .sku(sku)
            .price(29900)
            .stock(stock)
            .optionValues(List.of(colorValue, sizeValue))
            .displayName(colorValue + " / " + sizeValue)
            .isDefault(isDefault)
            .build();
    }

    @Nested
    @DisplayName("상품 생성 성공 테스트")
    class CreateProductTest {

        @Test
        @DisplayName("속성을 포함한 옵션 없는 상품을 정상적으로 생성한다.")
        void success_withNoneOption() {
            // given
            CreateProductRequest request = basicProductRequest()
                .productOptionType(ProductOptionType.NONE)
                .items(List.of(
                    ProductItemRequest.builder()
                        .sku("SKU-001")
                        .price(25000)
                        .stock(50)
                        .isDefault(true)
                        .attributes(List.of(
                            new ItemAttributeRequest(
                                testAttribute.getId(),
                                "소재: 면100%",
                                1
                            )
                        ))
                        .build()
                ))
                .build();

            // when
            CreatedProductResponse response = productService.createProduct(
                member.getId(), request
            );

            // then
            Product savedProduct = productRepository
                .findById(response.productId()).orElseThrow();

            ProductItem item = productItemRepository
                .findAllByProductId(savedProduct.getId()).getFirst();

            List<ProductAttribute> attributes = productAttributeRepository
                .findByItemIdOrdered(item.getId());

            assertThat(attributes).hasSize(1);

            ProductAttribute productAttribute = attributes.getFirst();
            assertThat(productAttribute.getAttributeId())
                .isEqualTo(testAttribute.getId());

            assertThat(productAttribute.getDisplayName()).isEqualTo("소재: 면100%");
            assertThat(productAttribute.getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("1개 옵션과 3개의 아이템을 포함한 상품을 정상적으로 생성한다.")
        void success_withSingleOption() {
            // given
            CreateProductRequest request = basicProductRequest()
                .productOptionType(ProductOptionType.SINGLE)
                .options(List.of(
                    OptionRequest.builder()
                        .optionOrder(1)
                        .optionId(colorOption.getId())
                        .name("색상")
                        .values(List.of(
                            OptionValueRequest.builder()
                                .value("화이트").displayOrder(1).build(),
                            OptionValueRequest.builder()
                                .value("블랙").displayOrder(2).build(),
                            OptionValueRequest.builder()
                                .value("그레이").displayOrder(3).build()
                        ))
                        .build()
                ))
                .items(List.of(
                    buildSingleOptionItem("SKU-SINGLE-WHITE-001", "화이트", 50, true),
                    buildSingleOptionItem("SKU-SINGLE-BLACK-001", "블랙", 30, false),
                    buildSingleOptionItem("SKU-SINGLE-GRAY-001", "그레이", 20, false)
                ))
                .build();

            // when
            CreatedProductResponse response = productService.createProduct(
                member.getId(), request
            );

            // then
            Product savedProduct = productRepository.findById(response.productId())
                .orElseThrow();

            assertThat(savedProduct.getOptionType()).isEqualTo(ProductOptionType.SINGLE);

            List<ProductOptionValue> savedOptions = productOptionValueRepository
                .findAllByProductId(savedProduct.getId());
            assertThat(savedOptions).hasSize(3);
            assertThat(savedOptions)
                .extracting(ProductOptionValue::getValue)
                .containsExactlyInAnyOrder("화이트", "블랙", "그레이");

            assertThat(savedOptions)
                .extracting(ProductOptionValue::getOptionOrder)
                .containsOnly(1);


            List<ProductItem> savedItems = productItemRepository
                .findAllByProductId(savedProduct.getId());
            assertThat(savedItems).hasSize(3);
            assertThat(savedItems)
                .allSatisfy(item -> {
                    assertThat(item.getOptionValue1()).isNotNull();
                    assertThat(item.getOptionValue2()).isNull();
                });

            List<ProductItem> defaultItems = savedItems.stream()
                .filter(ProductItem::isDefault)
                .toList();
            assertThat(defaultItems).hasSize(1);
            assertThat(defaultItems.getFirst().getSku())
                .isEqualTo("SKU-SINGLE-WHITE-001");

            int totalStock = savedItems.stream()
                .mapToInt(item -> item.getStock().getQuantity())
                .sum();
            assertThat(totalStock).isEqualTo(100);
        }

        @Test
        @DisplayName("2개 옵션으로 6개 아이템 상품을 정상적으로 생성한다.")
        void success_withDoubleOption() {
            // given
            CreateProductRequest request = basicProductRequest()
                .name("컬러 사이즈 티셔츠")
                .productOptionType(ProductOptionType.DOUBLE)
                .options(List.of(
                    OptionRequest.builder()
                        .optionOrder(1)
                        .optionId(colorOption.getId())
                        .name("색상")
                        .values(List.of(
                            OptionValueRequest.builder()
                                .value("화이트").displayOrder(1).build(),
                            OptionValueRequest.builder()
                                .value("블랙").displayOrder(2).build()
                        ))
                        .build(),
                    OptionRequest.builder()
                        .optionOrder(2)
                        .optionId(sizeOption.getId())
                        .name("사이즈")
                        .values(List.of(
                            OptionValueRequest.builder()
                                .value("S").displayOrder(1).build(),
                            OptionValueRequest.builder()
                                .value("M").displayOrder(2).build(),
                            OptionValueRequest.builder()
                                .value("L").displayOrder(3).build()
                        ))
                        .build()
                ))
                .items(List.of(
                    buildDoubleOptionItem("SKU-WHITE-S", "화이트", "S", 10, true),
                    buildDoubleOptionItem("SKU-WHITE-M", "화이트", "M", 20, false),
                    buildDoubleOptionItem("SKU-WHITE-L", "화이트", "L", 15, false),
                    buildDoubleOptionItem("SKU-BLACK-S", "블랙",   "S", 10, false),
                    buildDoubleOptionItem("SKU-BLACK-M", "블랙",   "M", 25, false),
                    buildDoubleOptionItem("SKU-BLACK-L", "블랙",   "L", 5, false)
                ))
                .build();

            // when
            CreatedProductResponse response = productService.createProduct(
                member.getId(), request
            );

            // then
            Product savedProduct = productRepository.findById(response.productId())
                .orElseThrow();

            assertThat(savedProduct.getOptionType()).isEqualTo(ProductOptionType.DOUBLE);

            List<ProductOptionValue> savedOptions = productOptionValueRepository
                .findAllByProductId(savedProduct.getId());
            assertThat(savedOptions).hasSize(5);

            List<ProductOptionValue> firstOptions = savedOptions.stream()
                .filter(o -> o.getOptionOrder() == 1)
                .toList();
            assertThat(firstOptions).hasSize(2);
            assertThat(firstOptions)
                .extracting(ProductOptionValue::getValue)
                .containsExactlyInAnyOrder("화이트", "블랙");

            List<ProductOptionValue> secondOptions = savedOptions.stream()
                .filter(o -> o.getOptionOrder() == 2)
                .toList();
            assertThat(secondOptions).hasSize(3);
            assertThat(secondOptions)
                .extracting(ProductOptionValue::getValue)
                .containsExactlyInAnyOrder("S", "M", "L");

            List<ProductItem> savedItems = productItemRepository
                .findAllByProductId(savedProduct.getId());
            assertThat(savedItems).hasSize(6);

            assertThat(savedItems).allSatisfy(item -> {
                assertThat(item.getOptionValue1()).isNotNull();
                assertThat(item.getOptionValue2()).isNotNull();
            });

            assertThat(savedItems)
                .extracting(ProductItem::getSku)
                .doesNotHaveDuplicates();

            assertThat(savedItems)
                .filteredOn(ProductItem::isDefault)
                .hasSize(1);

            assertThat(savedItems).allSatisfy(item ->
                    assertThat(item.getStock()).isNotNull()
            );
        }

    }

    @Nested
    @DisplayName("상품 생성 실패 테스트")
    class CreateProductFailTest {

        @Test
        @DisplayName("카테고리에 등록되지 않은 옵션 사용 시 예외가 발생한다.")
        void fail_notSupportedOption() {
            // given
            Option unsupportedOption = optionRepository.save(
                OptionFixture.createOption("별도", OptionInputType.RADIO)
            );

            CreateProductRequest request = basicProductRequest()
                .productOptionType(ProductOptionType.SINGLE)
                .options(List.of(
                    OptionRequest.builder()
                        .optionOrder(1)
                        .optionId(unsupportedOption.getId())
                        .name("핏")
                        .values(List.of(
                            OptionValueRequest.builder()
                                .value("오버핏").displayOrder(1).build()
                        ))
                        .build()
                ))
                .items(List.of(
                    buildSingleOptionItem("SKU-FAIL-OPTION-001", "오버핏", 10, true)
                ))
                .build();

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productService.createProduct(member.getId(), request));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.NOT_SUPPORTED_OPTION);
        }

        @Test
        @DisplayName("이미 존재하는 SKU로 상품 생성 요청 시 예외가 발생한다.")
        void fail_duplicatedSku() {
            // given
            CreateProductRequest firstRequest = basicProductRequest()
                .items(List.of(
                    ProductItemRequest.builder()
                        .sku("SKU-DUPLICATED-001")
                        .price(10000)
                        .stock(10)
                        .isDefault(true)
                        .build()
                ))
                .build();

            productService.createProduct(member.getId(), firstRequest);

            // when
            CreateProductRequest secondRequest = basicProductRequest()
                .items(List.of(
                    ProductItemRequest.builder()
                        .sku("SKU-DUPLICATED-001")
                        .price(20000)
                        .stock(5)
                        .isDefault(true)
                        .build()
                ))
                .build();

            // then
            assertThatThrownBy(
                () -> productService.createProduct(member.getId(), secondRequest)
            ).isInstanceOfAny(
                AppException.class,
                DataIntegrityViolationException.class
            );
        }

        @Test
        @DisplayName("다른 판매자의 상점에 상품 생성 시 예외가 발생한다.")
        void fail_productAccessDenied() {
            // given
            Member anotherMember = testFixture.createMember();
            Seller anotherSeller = testFixture.createSeller(anotherMember);
            Store anotherStore = testFixture.createStore(anotherSeller.getId());

            CreateProductRequest request = basicProductRequest()
                .storeId(anotherStore.getId())
                .categoryId(category.getId())
                .name("접근 불가 상품")
                .items(List.of(
                    ProductItemRequest.builder()
                        .sku("SKU-AUTH-FAIL-001")
                        .price(10000)
                        .stock(10)
                        .isDefault(true)
                        .build()
                ))
                .build();

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productService.createProduct(member.getId(), request));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

    }


}
