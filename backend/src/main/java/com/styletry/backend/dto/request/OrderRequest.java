package com.styletry.backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String shippingAddress;
    private List<OrderItemRequest> orderItems;
}
