package com.styletry.backend.service;

import com.styletry.backend.dto.request.OrderRequest;
import com.styletry.backend.dto.response.OrderItemResponse;
import com.styletry.backend.dto.response.OrderResponse;
import com.styletry.backend.model.Order;
import com.styletry.backend.model.OrderItem;
import com.styletry.backend.model.Product;
import com.styletry.backend.model.User;
import com.styletry.backend.model.Variant;
import com.styletry.backend.repository.OrderRepository;
import com.styletry.backend.repository.ProductRepository;
import com.styletry.backend.repository.UserRepository;
import com.styletry.backend.repository.VariantRepository;
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
    private final VariantRepository variantRepository;

    @Override
    public OrderResponse placeOrder(OrderRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(request.getShippingAddress());

        List<OrderItem> items = request.getOrderItems().stream().map(itemRequest -> {
            Variant variant = null;
            Product product;
            Double unitPrice;

            if (itemRequest.getVariantId() != null) {
                variant = variantRepository.findById(itemRequest.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Variant not found"));
                product = variant.getProduct();
                unitPrice = variant.getPrice();
            } else {
                product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                unitPrice = product.getPrice();
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setVariant(variant);
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(unitPrice * itemRequest.getQuantity());
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

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return mapToResponse(orderRepository.save(order));
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setCreatedAt(order.getCreatedAt());
        response.setCustomerName(order.getUser().getFullName());
        response.setCustomerEmail(order.getUser().getEmail());
        response.setItems(order.getOrderItems().stream().map(item -> {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setId(item.getId());
            itemResponse.setProductId(item.getProduct().getId());
            itemResponse.setVariantId(item.getVariant() != null ? item.getVariant().getId() : null);
            itemResponse.setProductName(item.getProduct().getName());
            itemResponse.setSize(item.getVariant() != null ? item.getVariant().getSize() : item.getProduct().getSize());
            itemResponse.setColor(item.getVariant() != null ? item.getVariant().getColor() : item.getProduct().getColor());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setSubtotal(item.getPrice());
            itemResponse.setPrice(item.getQuantity() > 0 ? item.getPrice() / item.getQuantity() : item.getPrice());
            return itemResponse;
        }).collect(Collectors.toList()));
        return response;
    }
}
