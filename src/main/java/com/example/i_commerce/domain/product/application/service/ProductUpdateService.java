package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.product.presentation.request.UpdateProductRequest;
import com.example.i_commerce.domain.product.presentation.request.UpdateProductStatusRequest;
import com.example.i_commerce.domain.product.presentation.response.UpdateProductStatusResponse;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductUpdateService {

    private final StoreService storeService;
    private final ProductRepository productRepository;

    public void updateBasicInfo(
        Long productId,
        Long userId,
        UpdateProductRequest request
    ) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if(!storeService.isStoreManager(userId, product.getStoreId())) {
            throw new AppException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

        product.updateBasicInfo(request.name(),request.description());
    }

    public UpdateProductStatusResponse changeProductStatus(
        Long productId,
        Long userId,
        UpdateProductStatusRequest request
    ) {
        Product product = ( request.status().requiresItemCascade()
            ? productRepository.findByIdWithItemsAndStock(productId)
            : productRepository.findById(productId)
        ).orElseThrow(() -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));

        if(!storeService.isStoreManager(userId, product.getStoreId())) {
            throw new AppException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

        product.changeStatus(request.status());
        return UpdateProductStatusResponse.from(product);
    }

}
