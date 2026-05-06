package com.example.i_commerce.domain.product.application.service;


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
import com.example.i_commerce.domain.product.repository.ProductAttributeRepository;
import com.example.i_commerce.domain.product.repository.ProductImageRepository;
import com.example.i_commerce.domain.product.repository.ProductQueryRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {

    private final OptionGroupBuilder optionGroupBuilder;
    private final OptionLookupBuilder optionLookupBuilder;

    private final ProductQueryRepository productQueryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductAttributeRepository productAttributeRepository;


    public ProductDetailResponse getProductDetail(Long productId, Long itemId) {

        Product product = productQueryRepository.findProductWithItems(productId)
            .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

        List<ProductImage> images = productImageRepository.findAllByProductId(productId);

        List<ProductItem> allItems = product.getItems();

        ProductItem selectedItem = product.findItemOrDefault(itemId);

        List<ProductAttribute> attributes = productAttributeRepository
            .findByItemIdOrdered(selectedItem.getId());

        List<ProductOptionGroupResponse> optionGroups = optionGroupBuilder.build(
            product.getOptionType(), product.getOptions(), allItems, selectedItem
        );

        OptionItemLookupResponse optionItemLookup = optionLookupBuilder.build(
            product.getOptionType(), allItems
        );

        return ProductDetailResponse.of(
            product, images, selectedItem, attributes, optionGroups, optionItemLookup
        );
    }

}
