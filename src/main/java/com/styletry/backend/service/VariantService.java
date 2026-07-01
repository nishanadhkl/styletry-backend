package com.styletry.backend.service;

import com.styletry.backend.dto.request.VariantRequest;
import com.styletry.backend.dto.response.VariantResponse;
import java.util.List;

public interface VariantService {
    VariantResponse createVariant(VariantRequest request);
    VariantResponse getVariantById(Long id);
    List<VariantResponse> getVariantsByProductId(Long productId);
    VariantResponse updateVariant(Long id, VariantRequest request);
    void deleteVariant(Long id);
}