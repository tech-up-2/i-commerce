package com.example.i_commerce.domain.product.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.example.i_commerce.domain.product.application.service.AttributeService;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.AttributeFixture;
import com.example.i_commerce.domain.product.presentation.request.CreateAttributeRequest;
import com.example.i_commerce.domain.product.presentation.response.AttributeGroupResponse;
import com.example.i_commerce.domain.product.repository.AttributeRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Attribute Service Unit Test")
public class AttributeServiceTest {

    @InjectMocks
    private AttributeService attributeService;

    @Mock
    private AttributeRepository attributeRepository;

    @Captor
    private ArgumentCaptor<List<Attribute>> attributeCaptor;


    @Nested
    @DisplayName("속성 생성 테스트")
    class CreateAttributeTest {

        private CreateAttributeRequest basieCreateAttributeRequest() {
            return new CreateAttributeRequest("key", List.of("value1", "value2"));
        }
        
        @Test
        @DisplayName("속성이 정상적으로 생성된다.")
        void createAttribute_success(){
            // given
            CreateAttributeRequest request = basieCreateAttributeRequest();
            given(attributeRepository.existsByKey(request.key()))
                .willReturn(false);
        
            // when
            attributeService.createAttribute(request);
        
            // then
            then(attributeRepository).should().saveAll(attributeCaptor.capture());

            List<Attribute> saved = attributeCaptor.getValue();
            assertThat(saved).hasSize(request.values().size());
            assertThat(saved).extracting(Attribute::getKey).containsOnly(request.key());
        }
        
        @Test
        @DisplayName("요청된 key가 이미 존재한다면 예외가 발생한다.")
        void createAttribute_fail_duplicateAttributeKey(){
            // given
            CreateAttributeRequest request = basieCreateAttributeRequest();
            given(attributeRepository.existsByKey(request.key()))
                .willReturn(true);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                attributeService.createAttribute(request));

            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.DUPLICATE_ATTRIBUTE_KEY);
            then(attributeRepository).should(never()).saveAll(any());
        }

    }

    @Nested
    @DisplayName("속성 그룹 조회 테스트")
    class GetAllAttributesGroupedByKeyTest {
        
        @Test
        @DisplayName("속성이 그룹화되어 정상적으로 조회된다.")
        void getAllAttributesGroupedByKey_success(){
            // given
            List<Attribute> attributes = AttributeFixture.createAttributes(
                "소재", List.of("면", "폴리에스터")
            );

            given(attributeRepository.findAllOrderedByKeyAndValue())
                .willReturn(attributes);

            // when
            List<AttributeGroupResponse> result =
                attributeService.getAllAttributesGroupedByKey();
        
            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().key()).isEqualTo("소재");
        }
    }

}
