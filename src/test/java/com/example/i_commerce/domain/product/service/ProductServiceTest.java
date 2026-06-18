package com.example.i_commerce.domain.product.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;


import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.product.application.service.ProductService;
import com.example.i_commerce.domain.product.application.validator.ProductValidator;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.CategoryFixture;
import com.example.i_commerce.domain.product.fixture.ProductFixture;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest;
import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.presentation.response.CreatedProductResponse;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 테스트")
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private StoreService storeService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductValidator productValidator;

    @Nested
    @DisplayName("상품 생성 테스트")
    class CreateProductTest {

        @Test
        @DisplayName("옵션 없는 상품을 정상적으로 생성한다.")
        void createProduct_WithNoneOption_Success() {
            // given
            Long userId = 1L;
            Long storeId = 1L;
            Long categoryId = 1L;
            CreateProductRequest request = ProductFixture.createProductRequest(
                storeId, categoryId, ProductOptionType.NONE
            );

            Product savedProduct = ProductFixture.createProduct(1L, storeId, ProductOptionType.NONE);

            given(storeService.isStoreManager(userId, storeId)).willReturn(true);
            given(categoryRepository.findById(categoryId))
                .willReturn(Optional.of(CategoryFixture.createRootWithId(categoryId)));
            given(productRepository.save(any(Product.class))).willReturn(savedProduct);

            // when
            CreatedProductResponse response = productService.createProduct(userId, request);

            // then
            assertThat(response).isNotNull();
            then(productRepository).should().save(any(Product.class));
        }

        @Test
        @DisplayName("상점 관리자가 아니면 예외가 발생한다.")
        void createProduct_fail_productAccessDenied() {
            // given
            Long userId = 1L;
            Long storeId = 1L;

            CreateProductRequest request = ProductFixture.createProductRequest(
                storeId, 1L, ProductOptionType.NONE
            );

            given(storeService.isStoreManager(userId, storeId)).willReturn(false);

            // when & then
            AppException exception = assertThrows(AppException.class,
                () -> productService.createProduct(userId, request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_ACCESS_DENIED);

            then(categoryRepository).should(never()).findById(any());
            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 카테고리 ID로 요청시 예외가 발생한다.")
        void createProduct_fail_categoryNotFound() {
            // given
            Long userId = 1L;
            Long storeId = 1L;
            Long categoryId = 999L;

            CreateProductRequest request = ProductFixture.createProductRequest(
                storeId, categoryId, ProductOptionType.NONE
            );

            given(storeService.isStoreManager(userId, storeId)).willReturn(true);
            given(categoryRepository.findById(categoryId))
                .willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class,
                () -> productService.createProduct(userId, request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.CATEGORY_NOT_FOUND);

            then(productValidator).should(never()).validateOptions(any(), any(), any());
            then(productRepository).should(never()).save(any());
        }

    }

}

