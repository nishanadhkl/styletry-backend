package com.styletry.backend.service;

import com.styletry.backend.dto.request.OrderRequest;
import com.styletry.backend.dto.response.OrderResponse;
import com.styletry.backend.model.Order;
import com.styletry.backend.model.OrderItem;
import com.styletry.backend.model.Product;
import com.styletry.backend.model.User;
import com.styletry.backend.repository.OrderRepository;
import com.styletry.backend.repository.ProductRepository;
import com.styletry.backend.repository.UserRepository;
import com.styletry.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderResponse placeOrder(OrderRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());

        List<OrderItem> items = request.getOrderItems().stream().map(itemRequest -> {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(product.getPrice() * itemRequest.getQuantity());
            return item;
        }).collect(Collectors.toList());

        order.setOrderItems(items);
        order.setTotalAmount(items.stream().mapToDouble(OrderItem::getPrice).sum());

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    @Override
    public List<OrderResponse> getUserOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}
