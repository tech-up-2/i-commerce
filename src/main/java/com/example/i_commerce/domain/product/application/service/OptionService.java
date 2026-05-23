package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.controller.request.CreateOptionRequest;
import com.example.i_commerce.domain.product.controller.response.OptionResponse;
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
        if(optionRepository.existsByName(request.name())) {
            throw new AppException(ProductErrorCode.DUPLICATE_OPTION_NAME);
        }

        optionRepository.save(Option.of(request.name(), request.inputType()));
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getAllOptions() {
        List<Option> options = optionRepository.findAllOrderedByName();

        return options.stream().map(o -> OptionResponse.of(
            o.getId(), o.getName(), o.getInputType()
        )).toList();

    }

    @Transactional
    public void deleteOption(Long optionId) {
        Option option = optionRepository.findById(optionId)
            .orElseThrow(() -> new AppException(ProductErrorCode.OPTION_NOT_FOUND));

        optionRepository.delete(option);
    }

}

