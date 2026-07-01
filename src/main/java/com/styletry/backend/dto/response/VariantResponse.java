package com.styletry.backend.dto.response;

import lombok.Data;

@Data
public class VariantResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String size;
    private String color;
    private Double price;
    private Integer stockQuantity;
    private String sku;
    private String imageUrl;
    private Boolean active;
}
