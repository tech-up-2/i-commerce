package com.example.i_commerce.domain.product.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.product.application.service.option.OptionGroupBuilder;
import com.example.i_commerce.domain.product.application.service.option.OptionLookupBuilder;
import com.example.i_commerce.domain.product.controller.response.OptionItemLookupResponse;
import com.example.i_commerce.domain.product.controller.response.ProductDetailResponse;
import com.example.i_commerce.domain.product.controller.response.ProductOptionGroupResponse;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductImage;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.ProductFixture;
import com.example.i_commerce.domain.product.repository.ProductAttributeRepository;
import com.example.i_commerce.domain.product.repository.ProductImageRepository;
import com.example.i_commerce.domain.product.repository.ProductQueryRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductQueryServiceTest {

    @InjectMocks
    private ProductQueryService productQueryService;

    @Mock
    private ProductQueryRepository productQueryRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductAttributeRepository productAttributeRepository;

    @Mock
    private OptionGroupBuilder optionGroupBuilder;

    @Mock
    private OptionLookupBuilder optionLookupBuilder;

    @Nested
    @DisplayName("상품 조회 성공 테스트")
    class GetProductSuccessTest {

        @Test
        @DisplayName("itemId가 null이면 기본 아이템 기준으로 상품 상세를 반환한다.")
        void getProductDetail_withNullItemId_returnsDefaultItem() {
            // given
            Product product = ProductFixture.createSingleOptionProduct();
            ProductItem defaultItem = product.getItems().getFirst();
            Long productId = product.getId();

            List<ProductImage> images = product.getImages();
            List<ProductAttribute> attributes = defaultItem.getAttributes();
            List<ProductOptionGroupResponse> optionGroups = List.of();
            OptionItemLookupResponse optionItemLookup = OptionItemLookupResponse.ofNone();

            given(productQueryRepository.findProductWithItems(productId))
                .willReturn(Optional.of(product));
            given(productImageRepository.findAllByProductId(productId))
                .willReturn(images);
            given(productAttributeRepository.findByItemIdOrdered(defaultItem.getId()))
                .willReturn(attributes);
            given(optionGroupBuilder.build(
                product.getOptionType(), product.getOptions(), product.getItems(), defaultItem)
            ).willReturn(optionGroups);
            given(optionLookupBuilder.build(product.getOptionType(), product.getItems()))
                .willReturn(optionItemLookup);

            // when
            ProductDetailResponse response = productQueryService.getProductDetail(productId, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.productId()).isEqualTo(defaultItem.getId());
        }

        @Test
        @DisplayName("itemId를 명시하면 해당 아이템 기준으로 상품 상세를 반환한다.")
        void getProductDetail_withItemId_returnsSpecificItem() {
            // given
            Product product = ProductFixture.createSingleOptionProduct();
            ProductItem targetItem = product.getItems().getFirst();
            Long productId = product.getId();
            Long itemId = targetItem.getId();

            List<ProductImage> images = product.getImages();
            List<ProductAttribute> attributes = targetItem.getAttributes();
            List<ProductOptionGroupResponse> optionGroups = List.of();
            OptionItemLookupResponse optionItemLookup = OptionItemLookupResponse.ofNone();

            given(productQueryRepository.findProductWithItems(productId))
                .willReturn(Optional.of(product));
            given(productImageRepository.findAllByProductId(productId))
                .willReturn(images);
            given(productAttributeRepository.findByItemIdOrdered(itemId))
                .willReturn(attributes);
            given(optionGroupBuilder.build(
                product.getOptionType(), product.getOptions(), product.getItems(), targetItem)
            ).willReturn(optionGroups);
            given(optionLookupBuilder.build(product.getOptionType(), product.getItems()))
                .willReturn(optionItemLookup);

            // when
            ProductDetailResponse response = productQueryService.getProductDetail(productId, itemId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.selectedItem().itemId()).isEqualTo(itemId);
        }

    }

    @Nested
    @DisplayName("상품 조회 실패 테스트")
    class GetProductFailTest {

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 PRODUCT_NOT_FOUND 예외가 발생한다.")
        void getProductDetail_productNotFound_throwsException() {
            // given

            Long productId = 999L;
            given(productQueryRepository.findProductWithItems(productId))
                .willReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> productQueryService.getProductDetail(productId, null))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

    }

}












