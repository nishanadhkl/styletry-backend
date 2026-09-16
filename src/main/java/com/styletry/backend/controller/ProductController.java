package com.styletry.backend.controller;

import com.styletry.backend.dto.request.ProductRequest;
import com.styletry.backend.dto.response.PageResponse;
import com.styletry.backend.dto.response.ProductResponse;
import com.styletry.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.addProduct(request));
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(required = false) Boolean sale,
                                            @RequestParam(required = false) Boolean newest,
                                            @RequestParam(required = false, name = "q") String query) {
        if (page == null && size == null && category == null && sale == null && newest == null && query == null) {
            return ResponseEntity.ok(productService.getAllProducts());
        }

        PageRequest pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 12,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        PageResponse<ProductResponse> response = productService.getProductsPage(category, sale, newest, query, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/sale")
    public ResponseEntity<List<ProductResponse>> getSaleProducts() {
        return ResponseEntity.ok(productService.getSaleProducts());
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<List<ProductResponse>> getNewArrivals() {
        return ResponseEntity.ok(productService.getNewArrivals());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchProducts(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
