package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.controller.request.CreateAttributeRequest;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.AttributeRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeRepository attributeRepository;

    @Transactional
    public void createAttribute(CreateAttributeRequest request) {

        if(attributeRepository.existsByKey(request.key())) {
            throw new AppException(ProductErrorCode.DUPLICATE_ATTRIBUTE_KEY);
        }

        List<Attribute> attributes = request.values().stream()
            .map(value -> Attribute.of(request.key(), value))
            .toList();

        attributeRepository.saveAll(attributes);
    }

}
