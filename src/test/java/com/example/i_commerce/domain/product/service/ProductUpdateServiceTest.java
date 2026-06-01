package com.example.i_commerce.domain.product.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.product.application.service.ProductUpdateService;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.presentation.request.UpdateProductRequest;
import com.example.i_commerce.domain.product.presentation.request.UpdateProductStatusRequest;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Update Service Unit Test")
public class ProductUpdateServiceTest {

    @InjectMocks
    private ProductUpdateService productUpdateService;

    @Mock
    private StoreService storeService;

    @Mock
    private ProductRepository productRepository;

    private static final Long PRODUCT_ID = 1L;
    private static final Long STORE_ID = 100L;
    private static final Long STORE_MANAGER_ID = 10L;
    private static final Long NON_MANAGER_ID = 20L;

    private Product product;

    @BeforeEach
    void setUp() {
        product = mock(Product.class);
    }

    private UpdateProductRequest basicProductUpdateRequest() {
        return new UpdateProductRequest("new 상품명", "new 상품 설명");
    }

    @Nested
    @DisplayName("상품 정보 수정 성공 테스트")
    class UpdateProductSuccessTest {

        @Test
        @DisplayName("상품 기본 정보를 정상적으로 수정한다.")
        public void updateBasicInfo_success(){
            // given
            UpdateProductRequest request = basicProductUpdateRequest();

            given(productRepository.findById(PRODUCT_ID))
                .willReturn(Optional.of(product));
            given(product.getStoreId()).willReturn(STORE_ID);
            given(storeService.isStoreManager(STORE_MANAGER_ID, STORE_ID))
                .willReturn(true);

            // when
            productUpdateService.updateBasicInfo(PRODUCT_ID, STORE_MANAGER_ID, request);

            // then
            then(product).should(times(1))
                .updateBasicInfo(request.name(), request.description());
        }

    }

    @Nested
    @DisplayName("상품 정보 수정 실패 테스트")
    class UpdateProductFailTest {

        @Test
        @DisplayName("존재하지 않는 상품일 경우 예외가 발생한다.")
        public void updateBasicInfo_fail_productNotFound(){
            // given
            UpdateProductRequest request = basicProductUpdateRequest();

            given(productRepository.findById(PRODUCT_ID))
                .willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productUpdateService.updateBasicInfo(PRODUCT_ID, STORE_MANAGER_ID, request)
            );
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);

            then(storeService).should(never()).isStoreManager(any(), any());
            then(product).should(never()).updateBasicInfo(any(), any());
        }

        @Test
        @DisplayName("상품 관리자가 아니라면 예외가 발생한다.")
        public void updateBasicInfo_fail_accessDenied(){
            // given
            UpdateProductRequest request = basicProductUpdateRequest();

            given(productRepository.findById(PRODUCT_ID))
                .willReturn(Optional.of(product));
            given(product.getStoreId()).willReturn(STORE_ID);
            given(storeService.isStoreManager(NON_MANAGER_ID, STORE_ID))
                .willReturn(false);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productUpdateService.updateBasicInfo(PRODUCT_ID, NON_MANAGER_ID, request)
            );
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_ACCESS_DENIED);

            then(product).should(never()).updateBasicInfo(any(), any());
        }

    }

    @Nested
    @DisplayName("상품 상태 변경 성공 테스트")
    class ChangeStatusSuccessTest {

        @Test
        @DisplayName("ON_SALE 상태로 변경한다.")
        public void changeProductStatus_success_toOnSale(){
            // given
            UpdateProductStatusRequest request =
                new UpdateProductStatusRequest(ProductStatus.ON_SALE);

            given(productRepository.findById(PRODUCT_ID))
                .willReturn(Optional.of(product));
            given(product.getStoreId()).willReturn(STORE_ID);
            given(storeService.isStoreManager(STORE_MANAGER_ID, STORE_ID))
                .willReturn(true);

            // when
            productUpdateService.changeProductStatus(PRODUCT_ID, STORE_MANAGER_ID, request);

            // then
            then(productRepository).should(times(1))
                .findById(PRODUCT_ID);
            then(productRepository).should(never()).findByIdWithItemsAndStock(any());
            then(product).should(times(1))
                .changeStatus(ProductStatus.ON_SALE);
        }

        @Test
        @DisplayName("PENDING 상태로 변경한다.")
        public void changeProductStatus_success_toPending(){
            // given
            UpdateProductStatusRequest request =
                new UpdateProductStatusRequest(ProductStatus.PENDING);

            given(productRepository.findById(PRODUCT_ID))
                .willReturn(Optional.of(product));
            given(product.getStoreId()).willReturn(STORE_ID);
            given(storeService.isStoreManager(STORE_MANAGER_ID, STORE_ID))
                .willReturn(true);

            // when
            productUpdateService.changeProductStatus(PRODUCT_ID, STORE_MANAGER_ID, request);

            // then
            then(productRepository).should(times(1))
                .findById(PRODUCT_ID);
            then(productRepository).should(never()).findByIdWithItemsAndStock(any());
            then(product).should(times(1))
                .changeStatus(ProductStatus.PENDING);
        }

        @Test
        @DisplayName("DISCONTINUED 상태로 변경한다.")
        public void changeProductStatus_success_toDiscontinued(){
            // given
            UpdateProductStatusRequest request =
                new UpdateProductStatusRequest(ProductStatus.DISCONTINUED);

            given(productRepository.findByIdWithItemsAndStock(PRODUCT_ID))
                .willReturn(Optional.of(product));
            given(product.getStoreId()).willReturn(STORE_ID);
            given(storeService.isStoreManager(STORE_MANAGER_ID, STORE_ID))
                .willReturn(true);

            // when
            productUpdateService.changeProductStatus(PRODUCT_ID, STORE_MANAGER_ID, request);

            // then
            then(productRepository).should(times(1))
                .findByIdWithItemsAndStock(PRODUCT_ID);
            then(productRepository).should(never()).findById(any());
            then(product).should(times(1))
                .changeStatus(ProductStatus.DISCONTINUED);
        }

    }

    @Nested
    @DisplayName("상품 상태 변경 실패 테스트")
    class ChangeStatusFailTest {

        @Test
        @DisplayName("cascade 불필요한 변경 시 존재하지 않는 상품일 경우 예외가 발생한다.")
        public void changeProductStaus_fail_productNotFound(){
            // given
            UpdateProductStatusRequest request =
                new UpdateProductStatusRequest(ProductStatus.ON_SALE);
            given(productRepository.findById(PRODUCT_ID))
                .willReturn(Optional.empty());
        
            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productUpdateService.changeProductStatus(PRODUCT_ID, STORE_MANAGER_ID, request)
            );
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);

            then(storeService).should(never()).isStoreManager(any(), any());
            then(product).should(never()).changeStatus(any());
        }

        @Test
        @DisplayName("cascade 필요한 변경 시 존재하지 않는 상품일 경우 예외가 발생한다.")
        public void changeProductStaus_fail_productNotFound_withCascade(){
            // given
            UpdateProductStatusRequest request =
                new UpdateProductStatusRequest(ProductStatus.DISCONTINUED);
            given(productRepository.findByIdWithItemsAndStock(PRODUCT_ID))
                .willReturn(Optional.empty());

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productUpdateService.changeProductStatus(PRODUCT_ID, STORE_MANAGER_ID, request)
            );
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);

            then(storeService).should(never()).isStoreManager(any(), any());
            then(product).should(never()).changeStatus(any());
        }
        
        @Test
        @DisplayName("상품 관리자가 아니라면 예외가 발생한다.")
        public void changeProductStatus_fail_accessDenied(){
            // given
            UpdateProductStatusRequest request =
                new UpdateProductStatusRequest(ProductStatus.ON_SALE);
            given(productRepository.findById(PRODUCT_ID))
                .willReturn(Optional.of(product));
            given(product.getStoreId()).willReturn(STORE_ID);
            given(storeService.isStoreManager(NON_MANAGER_ID, STORE_ID))
                .willReturn(false);
        
            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                productUpdateService.changeProductStatus(PRODUCT_ID, NON_MANAGER_ID, request)
            );

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.PRODUCT_ACCESS_DENIED);

            then(product).should(never()).changeStatus(any());
        }

    }

}
