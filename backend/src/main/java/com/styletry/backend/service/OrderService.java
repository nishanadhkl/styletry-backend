package com.styletry.backend.service;

import com.styletry.backend.dto.request.OrderRequest;
import com.styletry.backend.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request, String email);
    List<OrderResponse> getUserOrders(String email);
}
