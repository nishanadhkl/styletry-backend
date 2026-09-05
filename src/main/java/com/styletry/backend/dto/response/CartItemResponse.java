package com.styletry.backend.dto.response;

import lombok.Data;

@Data
public class CartItemResponse {
    private Long id;
    private Long variantId;
    private Long productId;
    private String productName;
    private String size;
    private String color;
    private Double price;
    private Integer quantity;
    private String imageUrl;
    private Double subtotal;
}
