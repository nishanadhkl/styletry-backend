package com.styletry.backend.service;

import com.styletry.backend.dto.request.VariantRequest;
import com.styletry.backend.dto.response.VariantResponse;
import com.styletry.backend.model.Product;
import com.styletry.backend.model.Variant;
import com.styletry.backend.repository.ProductRepository;
import com.styletry.backend.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {

    private final VariantRepository variantRepository;
    private final ProductRepository productRepository;

    @Override
    public VariantResponse createVariant(VariantRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Variant variant = new Variant();
        variant.setProduct(product);
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setSku(request.getSku());
        variant.setImageUrl(request.getImageUrl());
        variant.setActive(request.getActive() != null ? request.getActive() : true);
        return mapToResponse(variantRepository.save(variant));
    }

    @Override
    public VariantResponse getVariantById(Long id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        return mapToResponse(variant);
    }

    @Override
    public List<VariantResponse> getVariantsByProductId(Long productId) {
        return variantRepository.findByProductId(productId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public VariantResponse updateVariant(Long id, VariantRequest request) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setSku(request.getSku());
        variant.setImageUrl(request.getImageUrl());
        variant.setActive(request.getActive() != null ? request.getActive() : true);
        return mapToResponse(variantRepository.save(variant));
    }

    @Override
    public void deleteVariant(Long id) {
        variantRepository.deleteById(id);
    }

    private VariantResponse mapToResponse(Variant variant) {
        VariantResponse response = new VariantResponse();
        response.setId(variant.getId());
        response.setProductId(variant.getProduct().getId());
        response.setProductName(variant.getProduct().getName());
        response.setSize(variant.getSize());
        response.setColor(variant.getColor());
        response.setPrice(variant.getPrice());
        response.setStockQuantity(variant.getStockQuantity());
        response.setSku(variant.getSku());
        response.setImageUrl(variant.getImageUrl());
        response.setActive(variant.getActive());
        return response;
    }
}