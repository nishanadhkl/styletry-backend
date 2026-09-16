package com.styletry.backend.service;

import com.styletry.backend.dto.request.OrderRequest;
import com.styletry.backend.dto.response.OrderResponse;
import com.styletry.backend.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request, String email);
    List<OrderResponse> getUserOrders(String email);
    PageResponse<OrderResponse> getUserOrdersPage(String email, Pageable pageable);
    List<OrderResponse> getAllOrders();
    PageResponse<OrderResponse> getAllOrdersPage(Pageable pageable);
    OrderResponse updateOrderStatus(Long id, String status);
    OrderResponse cancelOrder(Long id, String email);
}
