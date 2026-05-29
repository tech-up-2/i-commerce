package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.application.dto.ProductItemInfo;
import com.example.i_commerce.domain.product.application.mapper.OptionGroupBuilder;
import com.example.i_commerce.domain.product.application.mapper.OptionLookupBuilder;
import com.example.i_commerce.domain.product.application.dto.OptionItemLookupDto;
import com.example.i_commerce.domain.product.presentation.response.ProductDetailResponse;
import com.example.i_commerce.domain.product.application.dto.ProductOptionGroupDto;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductImage;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.ProductAttributeRepository;
import com.example.i_commerce.domain.product.repository.ProductImageRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.repository.projection.ProductItemInfoProjection;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.domain.product.repository.ProductQueryRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService  {

    private final OptionGroupBuilder optionGroupBuilder;
    private final OptionLookupBuilder optionLookupBuilder;

    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductItemRepository productItemRepository;
    private final ProductAttributeRepository productAttributeRepository;


    public List<Long> getProductIdsByStoreIds(List<Long> storeIds) {
        return productRepository.findAllIdsByStoreIds(storeIds);
    }

    public Long getStoreIdByProductId(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND))
            .getStoreId();
    }

    public ProductItemInfo getProductItemInfoById(Long itemId) {
        ProductItemInfoProjection itemInfoProjection = productItemRepository.findItemInfoById(itemId)
            .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_ITEM_NOT_FOUND));

        return ProductItemInfo.from(itemInfoProjection);
    }

    public List<ProductItemInfo> getProductItemInfosByIds(Set<Long> productItemIds) {
        if (productItemIds.isEmpty()) {
            return List.of();
        }

        List<ProductItemInfoProjection> infoProjections = productItemRepository
            .findAllItemInfoByIdIn(productItemIds);

        return infoProjections.stream()
            .map(ProductItemInfo::from)
            .toList();
    }

    public ProductDetailResponse getProductDetail(Long productId, Long itemId) {

        Product product = productQueryRepository.findProductWithItems(productId)
            .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

        List<ProductImage> images = productImageRepository.findAllByProductId(productId);

        List<ProductItem> allItems = product.getItems();

        ProductItem selectedItem = product.findItemOrDefault(itemId);

        List<ProductAttribute> attributes = productAttributeRepository
            .findByItemIdOrdered(selectedItem.getId());

        List<ProductOptionGroupDto> optionGroups = optionGroupBuilder.build(
            product.getOptionType(), product.getOptions(), allItems, selectedItem
        );

        OptionItemLookupDto optionItemLookup = optionLookupBuilder.build(
            product.getOptionType(), allItems
        );

        return ProductDetailResponse.of(
            product, images, selectedItem, attributes, optionGroups, optionItemLookup
        );
    }

}
