package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.controller.request.CreateOptionRequest;
import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.OptionRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OptionService {

    private final OptionRepository optionRepository;

    @Transactional
    public void createOption(CreateOptionRequest request) {

        if(optionRepository.existsByType(request.type())) {
            throw new AppException(ProductErrorCode.DUPLICATE_OPTION_TYPE);
        }

        List<Option> options = request.values().stream()
            .map(value -> Option.of(
                request.type(),
                value,
                request.inputType())
            ).toList();

        optionRepository.saveAll(options);
    }


}

