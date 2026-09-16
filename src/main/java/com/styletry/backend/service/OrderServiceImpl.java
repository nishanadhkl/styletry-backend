package com.styletry.backend.service;

import com.styletry.backend.dto.request.OrderRequest;
import com.styletry.backend.dto.response.OrderItemResponse;
import com.styletry.backend.dto.response.OrderResponse;
import com.styletry.backend.dto.response.PageResponse;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    private final EmailService emailService;

    @Override
    @Transactional
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
                validateStock(variant, itemRequest.getQuantity());
                variant.setStockQuantity(variant.getStockQuantity() - itemRequest.getQuantity());
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

        items.stream()
                .map(OrderItem::getVariant)
                .filter(savedVariant -> savedVariant != null)
                .forEach(variantRepository::save);

        Order saved = orderRepository.save(order);
        emailService.sendOrderPlacedEmail(saved);
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
    public PageResponse<OrderResponse> getUserOrdersPage(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return PageResponse.from(orderRepository.findByUser(user, pageable).map(this::mapToResponse));
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<OrderResponse> getAllOrdersPage(Pageable pageable) {
        return PageResponse.from(orderRepository.findAll(pageable).map(this::mapToResponse));
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String currentStatus = order.getStatus() == null ? "" : order.getStatus().toUpperCase();
        String nextStatus = status == null ? "" : status.toUpperCase();

        if ("DELIVERED".equals(currentStatus) && !"DELIVERED".equals(nextStatus)) {
            throw new RuntimeException("Delivered orders cannot be changed to another status");
        }

        if ("CANCELLED".equals(currentStatus) && !"CANCELLED".equals(nextStatus)) {
            throw new RuntimeException("Cancelled orders cannot be changed to another status");
        }

        if ("CANCELLED".equals(nextStatus) && !"PENDING".equals(currentStatus)) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }

        if ("CANCELLED".equals(nextStatus)) {
            restoreStock(order);
        }

        order.setStatus(nextStatus);
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusEmail(saved);
        return mapToSummaryResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You can only cancel your own orders");
        }

        String currentStatus = order.getStatus() == null ? "" : order.getStatus().toUpperCase();
        if (!"PENDING".equals(currentStatus)) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }

        restoreStock(order);
        order.setStatus("CANCELLED");
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusEmail(saved);
        return mapToResponse(saved);
    }

    private OrderResponse mapToSummaryResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setCreatedAt(order.getCreatedAt());
        response.setCustomerName(order.getUser().getFullName());
        response.setCustomerEmail(order.getUser().getEmail());
        return response;
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

    private void validateStock(Variant variant, Integer requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity < 1) {
            throw new RuntimeException("Quantity must be at least 1");
        }

        int available = variant.getStockQuantity() == null ? 0 : variant.getStockQuantity();
        if (requestedQuantity > available) {
            throw new RuntimeException("Only " + available + " item(s) available in stock for "
                    + variant.getProduct().getName() + " (" + variant.getSize() + ", " + variant.getColor() + ")");
        }
    }

    private void restoreStock(Order order) {
        if (order.getOrderItems() == null) {
            return;
        }

        order.getOrderItems().stream()
                .filter(item -> item.getVariant() != null)
                .forEach(item -> {
                    Variant variant = item.getVariant();
                    int currentStock = variant.getStockQuantity() == null ? 0 : variant.getStockQuantity();
                    int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                    variant.setStockQuantity(currentStock + quantity);
                    variantRepository.save(variant);
                });
    }
}
