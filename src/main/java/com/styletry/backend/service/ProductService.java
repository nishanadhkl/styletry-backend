package com.styletry.backend.service;

import com.styletry.backend.dto.request.ProductRequest;
import com.styletry.backend.dto.response.PageResponse;
import com.styletry.backend.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponse addProduct(ProductRequest request);
    PageResponse<ProductResponse> getProductsPage(String category, Boolean saleOnly, Boolean newOnly, String query, Pageable pageable);
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    List<ProductResponse> getProductsByCategory(String category);
    List<ProductResponse> getSaleProducts();
    List<ProductResponse> getNewArrivals();
    List<ProductResponse> searchProducts(String name);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}
