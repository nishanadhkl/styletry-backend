package com.styletry.backend.dto.request;

import lombok.Data;

@Data
public class CartRequest {
    private Long variantId;
    private Integer quantity;
}
