package com.styletry.backend.dto.response;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String size;
    private String color;
    private Double price;
    private Integer quantity;
    private Double subtotal;
}
