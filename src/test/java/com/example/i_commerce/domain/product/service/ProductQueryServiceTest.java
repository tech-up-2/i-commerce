package com.example.i_commerce.domain.product.service;



import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.i_commerce.domain.product.application.dto.ProductItemInfo;
import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.product.application.mapper.OptionGroupBuilder;
import com.example.i_commerce.domain.product.application.mapper.OptionLookupBuilder;
import com.example.i_commerce.domain.product.application.dto.OptionItemLookupDto;
import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import com.example.i_commerce.domain.product.fixture.ProductItemFixture;
import com.example.i_commerce.domain.product.presentation.response.ProductDetailResponse;
import com.example.i_commerce.domain.product.application.dto.ProductOptionGroupDto;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductImage;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.ProductFixture;
import com.example.i_commerce.domain.product.repository.ProductAttributeRepository;
import com.example.i_commerce.domain.product.repository.ProductImageRepository;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.domain.product.repository.ProductQueryRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.repository.projection.ProductItemInfoProjection;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductQueryService unit test")
public class ProductQueryServiceTest {

    @InjectMocks
    private ProductQueryService productQueryService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductItemRepository productItemRepository;

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
    @DisplayName("상점 ID기반 상품 조회 테스트")
    class GetProductIdsByStoreIdsTest {

        @Test
        @DisplayName("스토어 ID 목록으로 상품 ID 목록을 반환한다")
        void getProductIdsByStoreIds_success() {
            // given
            List<Long> storeIds = List.of(1L, 2L);
            List<Long> productIds = List.of(1L, 2L, 3L);
            given(productRepository.findAllIdsByStoreIds(storeIds))
                .willReturn(productIds);

            // when
            List<Long> result = productQueryService.getProductIdsByStoreIds(storeIds);

            // then
            assertThat(result).isEqualTo(productIds);
            then(productRepository).should().findAllIdsByStoreIds(storeIds);
        }

    }

    @Nested
    @DisplayName("상품으로 상점 ID 조회 테스트")
    class GetStoreIdByProductIdTest {

        @Test
        @DisplayName("상품 ID로 스토어 ID를 정상적으로 반환한다.")
        void getStoreIdByProductId_success() {
            // given
            Long productId = 1L;
            Product product = ProductFixture.defaultProduct().build();
            given(productRepository.findById(productId)).willReturn(Optional.of(product));

            // when
            Long storeId = productQueryService.getStoreIdByProductId(productId);

            // then
            assertThat(storeId).isEqualTo(product.getStoreId());
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 요청시 예외가 발생한다")
        void getStoreIdByProductId_fail_productNotFound() {
            // given
            Long productId = 999L;
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productQueryService.getStoreIdByProductId(productId));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

    }

    @Nested
    @DisplayName("상품 ID로 상품 아이템 정보 조회 테스트")
    class GetProductItemInfoByIdTest {

        private ProductItemInfoProjection createProjectionWithStatus(
            ProductItemStatus status
        ) {
            return new ProductItemInfoProjection(
                1L, "상품명", 1L,
                1000, "", 100, status
            );
        }

        @Test
        @DisplayName("아이템 ID로 판매중인 상품 아이템 정보가 정상적으로 반환된다.")
        void getProductItemInfoById_success_onSale() {
            // given
            Long itemId = 1L;
            ProductItemInfoProjection projection =
                createProjectionWithStatus(ProductItemStatus.ON_SALE);

            given(productItemRepository.findItemInfoById(itemId))
                .willReturn(Optional.of(projection));

            // when
            ProductItemInfo result = productQueryService.getProductItemInfoById(itemId);

            // then
            assertThat(result.productItemId()).isEqualTo(itemId);
            assertThat(result.isOnSale()).isTrue();
        }

        @Test
        @DisplayName("아이템 ID로 판매중이 아닌 상품 아이템 정보가 정상적으로 반환된다.")
        void getProductItemInfoById_success_notOnSale() {
            // given
            Long itemId = 1L;
            ProductItemInfoProjection projection =
                createProjectionWithStatus(ProductItemStatus.OFF_SALE);

            given(productItemRepository.findItemInfoById(itemId))
                .willReturn(Optional.of(projection));

            // when
            ProductItemInfo result = productQueryService.getProductItemInfoById(itemId);

            // then
            assertThat(result.productItemId()).isEqualTo(itemId);
            assertThat(result.isOnSale()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 아이템 ID로 요청시 예외가 발생한다.")
        void getProductItemInfoById_fail_productItemNotFound() {
            // given
            Long itemId = 999L;
            given(productItemRepository.findItemInfoById(itemId))
                .willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productQueryService.getProductItemInfoById(itemId));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_ITEM_NOT_FOUND);

        }

    }


    @Nested
    @DisplayName("상품 ID 목록으로 상품 아이템 정보 목록 조회 테스트")
    class GetProductItemInfosByIdsTest {

        private ProductItemInfoProjection createProjection(
            Long id, String name
        ) {
            return new ProductItemInfoProjection(
                id, name, 1L, 1000, "", 100, ProductItemStatus.ON_SALE
            );
        }

        @Test
        @DisplayName("상품 아이템 정보 목록이 정상적으로 반환된다.")
        void getProductItemInfosByIds_success() {
            // given
            Set<Long> itemIds = Set.of(1L, 2L);
            List<ProductItemInfoProjection> projections = List.of(
                createProjection(1L, "상품A"),
                createProjection(2L, "상품B")
            );

            given(productItemRepository.findAllItemInfoByIdIn(itemIds))
                .willReturn(projections);

            // when
            List<ProductItemInfo> result =
                productQueryService.getProductItemInfosByIds(itemIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting(ProductItemInfo::productItemId)
                .containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("itemIds가 null이면 빈 리스트를 반환한다")
        void getProductItemInfosByIds_success_nullIds() {
            // when
            List<ProductItemInfo> result =
                productQueryService.getProductItemInfosByIds(null);

            // then
            assertThat(result).isEmpty();
            then(productItemRepository).should(never()).findAllItemInfoByIdIn(any());
        }

        @Test
        @DisplayName("itemIds가 비어있으면 빈 리스트를 반환한다")
        void getProductItemInfosByIds_success_emptyIds() {
            // when
            List<ProductItemInfo> result =
                productQueryService.getProductItemInfosByIds(Set.of());

            // then
            assertThat(result).isEmpty();
            then(productItemRepository).should(never()).findAllItemInfoByIdIn(any());
        }

    }


    @Nested
    @DisplayName("상품 상세 조회 테스트")
    class GetProductDetailTest {

        @Test
        @DisplayName("요청된 itemId가 없다면 기본 아이템 기준으로 상품 상세를 반환한다.")
        void getProductDetail_success_withNullItemId() {
            // given
            Long productId = 1L;
            ProductItem defaultItem = ProductItemFixture.createItem(1L, true);
            Product product = ProductFixture.createNoneOptionProduct(defaultItem);

            List<ProductImage> images = List.of();
            List<ProductAttribute> attributes = List.of();
            List<ProductOptionGroupDto> optionGroups = List.of();
            OptionItemLookupDto optionItemLookup = OptionItemLookupDto.ofNone();

            given(productQueryRepository.findProductWithItems(productId))
                .willReturn(Optional.of(product));
            given(productImageRepository.findAllByProductId(productId))
                .willReturn(images);
            given(productAttributeRepository.findByItemIdOrdered(defaultItem.getId()))
                .willReturn(attributes);
            given(optionGroupBuilder.build(
                product.getOptionType(), product.getOptions(),
                product.getItems(), defaultItem
            )).willReturn(optionGroups);
            given(optionLookupBuilder.build(product.getOptionType(), product.getItems()))
                .willReturn(optionItemLookup);

            // when
            ProductDetailResponse result =
                productQueryService.getProductDetail(productId, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.selectedItem().itemId()).isEqualTo(defaultItem.getId());
            then(productAttributeRepository).should()
                .findByItemIdOrdered(defaultItem.getId());
        }

        @Test
        @DisplayName("요청된 itemId가 있다면 해당 아이템 기준으로 상품 상세를 반환한다.")
        void getProductDetail_success_withItemId() {
            // given
            Long productId = 1L;
            Long itemId = 10L;
            ProductItem productItem = ProductItemFixture.createItem(itemId, false);
            Product product = ProductFixture.createNoneOptionProduct(productItem);

            List<ProductImage> images = List.of();
            List<ProductAttribute> attributes = List.of();
            List<ProductOptionGroupDto> optionGroups = List.of();
            OptionItemLookupDto optionItemLookup = OptionItemLookupDto.ofNone();

            given(productQueryRepository.findProductWithItems(productId))
                .willReturn(Optional.of(product));
            given(productImageRepository.findAllByProductId(productId))
                .willReturn(images);
            given(productAttributeRepository.findByItemIdOrdered(itemId))
                .willReturn(attributes);
            given(optionGroupBuilder.build(
                product.getOptionType(), product.getOptions(),
                product.getItems(), productItem
            )).willReturn(optionGroups);
            given(optionLookupBuilder.build(product.getOptionType(), product.getItems()))
                .willReturn(optionItemLookup);

            // when
            ProductDetailResponse response =
                productQueryService.getProductDetail(productId, itemId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.selectedItem().itemId()).isEqualTo(itemId);
            then(productAttributeRepository).should().findByItemIdOrdered(itemId);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 요청시 예외가 발생한다.")
        void getProductDetail_fail_productNotFound() {
            // given
            Long productId = 999L;
            given(productQueryRepository.findProductWithItems(productId))
                .willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productQueryService.getProductDetail(productId, null));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

    }

}












