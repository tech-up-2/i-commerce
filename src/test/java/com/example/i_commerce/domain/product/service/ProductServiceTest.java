package com.example.i_commerce.domain.product.service;


import static com.example.i_commerce.domain.product.fixture.AttributeFixture.colorAttribute;
import static com.example.i_commerce.domain.product.fixture.AttributeFixture.volumeAttribute1;
import static com.example.i_commerce.domain.product.fixture.AttributeFixture.volumeAttribute2;
import static com.example.i_commerce.domain.product.fixture.CategoryFixture.devicesCategory;
import static com.example.i_commerce.domain.product.fixture.CreateProductRequestFixture.doubleOptionRequest;
import static com.example.i_commerce.domain.product.fixture.CreateProductRequestFixture.noneOptionRequest;
import static com.example.i_commerce.domain.product.fixture.CreateProductRequestFixture.singleOptionRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.product.application.service.ProductService;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.ProductItemRequest;
import com.example.i_commerce.domain.product.controller.response.CreatedProductResponse;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductOptionType;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.application.validator.ProductAttributeValidator;
import com.example.i_commerce.domain.product.application.validator.ProductOptionValidator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private ProductOptionValidator optionValidator;

    @Mock
    private ProductAttributeValidator attributeValidator;


    @Nested
    @DisplayName("상품 생성 성공 테스트")
    class CreateProductSuccessTest {

        @Test
        @DisplayName("옵션이 없는 단일 상품을 생성한다.")
        void createNoOptionProduct() {
            // given
            Long sellerId = 1L;
            CreateProductRequest request = noneOptionRequest();
            Category category = devicesCategory();
            Store store = mock(Store.class);
            Map<Long, Attribute> attributeMap = Map.of(1L, colorAttribute());

            given(store.getSellerId()).willReturn(sellerId);
            given(storeService.isStoreManager(sellerId, store.getSellerId()))
                .willReturn(true);
            given(categoryRepository.findById(request.categoryId()))
                .willReturn(Optional.of(category));
            given(attributeValidator.validateAndFetchAttributes(request))
                .willReturn(attributeMap);
            given(productRepository.save(any(Product.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            CreatedProductResponse response = productService.createProduct(sellerId, request);

            // then
            then(optionValidator).should().validateOptions(request);
            then(attributeValidator).should().validateAndFetchAttributes(request);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            then(productRepository).should(times(2)).save(captor.capture());

            Product savedProduct = captor.getValue();

            assertThat(response).isNotNull();
            assertThat(response.productId()).isEqualTo(savedProduct.getId());

            assertThat(savedProduct.getOptionType()).isEqualTo(ProductOptionType.NONE);
            assertThat(savedProduct.getOptions()).isEmpty();

            assertProductItems(savedProduct, request);

        }

        @Test
        @DisplayName("단일 옵션을 가지는 상품을 생성한다.")
        void createSingleOptionProduct() {
            // given
            Long sellerId = 1L;
            CreateProductRequest request = singleOptionRequest();
            Category category = devicesCategory();
            Store store = mock(Store.class);
            Map<Long, Attribute> attributeMap = Map.of(1L, colorAttribute());

            given(store.getSellerId()).willReturn(sellerId);
            given(storeService.isStoreManager(sellerId, store.getSellerId()))
                .willReturn(true);
            given(categoryRepository.findById(request.categoryId()))
                .willReturn(Optional.of(category));
            given(attributeValidator.validateAndFetchAttributes(request))
                .willReturn(attributeMap);
            given(productRepository.save(any(Product.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            CreatedProductResponse response = productService.createProduct(sellerId, request);

            // then
            then(optionValidator).should().validateOptions(request);
            then(attributeValidator).should().validateAndFetchAttributes(request);

            ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
            then(productRepository).should(times(2)).save(productCaptor.capture());

            Product savedProduct = productCaptor.getValue();

            assertThat(response).isNotNull();
            assertThat(response.productId()).isEqualTo(savedProduct.getId());

            assertThat(savedProduct.getName()).isEqualTo(request.name());
            assertThat(savedProduct.getOptionType()).isEqualTo(ProductOptionType.SINGLE);

            assertThat(savedProduct.getOptions())
                .allMatch(option -> option.getOptionOrder() == 1);

            assertProductItems(savedProduct, request);

        }

        @Test
        @DisplayName("다중 옵션을 가지는 상품을 생성한다.")
        void createCombinationOptionProduct() {
            // given
            Long sellerId = 1L;
            CreateProductRequest request = doubleOptionRequest();
            Category category = devicesCategory();
            Store store = mock(Store.class);
            Map<Long, Attribute> attributeMap = Map.of(
                1L, colorAttribute(),
                3L, volumeAttribute1(),
                4L, volumeAttribute2()
            );

            given(store.getSellerId()).willReturn(sellerId);
            given(storeService.isStoreManager(sellerId, store.getSellerId()))
                .willReturn(true);
            given(categoryRepository.findById(request.categoryId()))
                .willReturn(Optional.of(category));
            given(attributeValidator.validateAndFetchAttributes(request))
                .willReturn(attributeMap);
            given(productRepository.save(any(Product.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

            // when
            CreatedProductResponse response = productService.createProduct(sellerId, request);

            // then
            then(optionValidator).should().validateOptions(request);
            then(attributeValidator).should().validateAndFetchAttributes(request);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            then(productRepository).should(times(2)).save(captor.capture());

            Product savedProduct = captor.getValue();

            assertThat(response).isNotNull();
            assertThat(response.productId()).isEqualTo(savedProduct.getId());

            assertThat(savedProduct.getName()).isEqualTo(request.name());
            assertThat(savedProduct.getOptionType()).isEqualTo(ProductOptionType.DOUBLE);

            assertProductItems(savedProduct, request);

        }

    }

    private void assertProductItems(Product product, CreateProductRequest request) {

        assertThat(product.getItems()).hasSize(request.items().size());

        Set<String> expectedSkus = request.items().stream()
            .map(ProductItemRequest::sku)
            .collect(Collectors.toSet());
        Set<String> actualSkus = product.getItems().stream()
            .map(ProductItem::getSku)
            .collect(Collectors.toSet());
        assertThat(actualSkus).isEqualTo(expectedSkus);

        assertThat(product.getItems())
            .allMatch(item -> item.getProduct() == product);

        assertThat(product.getItems())
            .allMatch(item -> item.getStock() != null);
    }


}









