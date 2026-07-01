package com.styletry.backend.dto.request;

import lombok.Data;

@Data
public class VariantRequest {
    private Long productId;
    private String size;
    private String color;
    private Double price;
    private Integer stockQuantity;
    private String sku;
    private String imageUrl;
    private Boolean active = true;
}
