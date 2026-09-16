package com.styletry.backend.controller;

import com.styletry.backend.dto.request.OrderRequest;
import com.styletry.backend.dto.response.PageResponse;
import com.styletry.backend.dto.response.OrderResponse;
import com.styletry.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.placeOrder(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<?> getUserOrders(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(orderService.getUserOrders(userDetails.getUsername()));
        }

        PageRequest pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        PageResponse<OrderResponse> response = orderService.getUserOrdersPage(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders(@RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(orderService.getAllOrders());
        }

        PageRequest pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return ResponseEntity.ok(orderService.getAllOrdersPage(pageable));
    }

    @PutMapping("/admin/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                           @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userDetails.getUsername()));
    }
}
