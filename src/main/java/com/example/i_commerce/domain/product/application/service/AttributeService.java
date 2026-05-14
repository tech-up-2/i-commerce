package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.controller.request.CreateAttributeRequest;
import com.example.i_commerce.domain.product.controller.response.AttributeGroupResponse;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.AttributeRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeRepository attributeRepository;

    @Transactional
    public void createAttribute(CreateAttributeRequest request) {

        if (attributeRepository.existsByKey(request.key())) {
            throw new AppException(ProductErrorCode.DUPLICATE_ATTRIBUTE_KEY);
        }

        List<Attribute> attributes = request.values().stream()
            .map(value -> Attribute.of(request.key(), value))
            .toList();

        attributeRepository.saveAll(attributes);
    }


    @Transactional(readOnly = true)
    public List<AttributeGroupResponse> getAllAttributesGroupedByKey() {
        List<Attribute> attributes = attributeRepository.findAllOrderedByKeyAndValue();

        Map<String, List<Attribute>> groupedAttributes = attributes.stream()
            .collect(Collectors.groupingBy(
                Attribute::getKey, LinkedHashMap::new, Collectors.toList())
            );

        return groupedAttributes.entrySet().stream()
            .map(entry ->
                AttributeGroupResponse.of(entry.getKey(), entry.getValue())
            ).toList();

    }

}


