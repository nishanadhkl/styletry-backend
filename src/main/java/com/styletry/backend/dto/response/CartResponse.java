package com.styletry.backend.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Double totalAmount;
}
