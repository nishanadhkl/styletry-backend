package com.styletry.backend.controller;

import com.styletry.backend.dto.request.VariantRequest;
import com.styletry.backend.dto.response.VariantResponse;
import com.styletry.backend.service.VariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class VariantController {

    private final VariantService variantService;

    @PostMapping
    public ResponseEntity<VariantResponse> createVariant(@RequestBody VariantRequest request) {
        return ResponseEntity.ok(variantService.createVariant(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VariantResponse> getVariantById(@PathVariable Long id) {
        return ResponseEntity.ok(variantService.getVariantById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<VariantResponse>> getVariantsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(variantService.getVariantsByProductId(productId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VariantResponse> updateVariant(@PathVariable Long id,
                                                         @RequestBody VariantRequest request) {
        return ResponseEntity.ok(variantService.updateVariant(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        variantService.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }
}
