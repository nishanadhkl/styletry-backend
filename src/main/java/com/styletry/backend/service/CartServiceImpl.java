package com.styletry.backend.service;

import com.styletry.backend.dto.request.CartRequest;
import com.styletry.backend.dto.response.CartItemResponse;
import com.styletry.backend.dto.response.CartResponse;
import com.styletry.backend.model.*;
import com.styletry.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final VariantRepository variantRepository;

    private Cart getOrCreateCart(String email) {
        return cartRepository.findByUserEmail(email).orElseGet(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    @Override
    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse addToCart(String email, CartRequest request) {
        Cart cart = getOrCreateCart(email);
        Variant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        // Check if variant already in cart
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getVariant().getId().equals(request.getVariantId()))
                .findFirst();

        int existingQuantity = existing.map(CartItem::getQuantity).orElse(0);
        int requestedQuantity = existingQuantity + request.getQuantity();
        validateStock(variant, requestedQuantity);

        if (existing.isPresent()) {
            existing.get().setQuantity(requestedQuantity);
            cartItemRepository.save(existing.get());
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setVariant(variant);
            item.setQuantity(request.getQuantity());
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        return mapToResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateCartItem(String email, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        validateStock(item.getVariant(), quantity);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        Cart cart = getOrCreateCart(email);
        return mapToResponse(cart);
    }

    @Override
    public void removeCartItem(String email, Long itemId) {
        cartItemRepository.deleteById(itemId);
    }

    @Override
    public void clearCart(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(item -> {
            CartItemResponse r = new CartItemResponse();
            r.setId(item.getId());
            r.setVariantId(item.getVariant().getId());
            r.setProductName(item.getVariant().getProduct().getName());
            r.setProductId(item.getVariant().getProduct().getId());
            r.setSize(item.getVariant().getSize());
            r.setColor(item.getVariant().getColor());
            r.setPrice(item.getVariant().getPrice());
            r.setQuantity(item.getQuantity());
            r.setImageUrl(item.getVariant().getImageUrl() != null
                    ? item.getVariant().getImageUrl()
                    : item.getVariant().getProduct().getImageUrl());
            r.setSubtotal(item.getVariant().getPrice() * item.getQuantity());
            return r;
        }).collect(Collectors.toList());

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setItems(items);
        response.setTotalItems(items.stream().mapToInt(CartItemResponse::getQuantity).sum());
        response.setTotalAmount(items.stream().mapToDouble(CartItemResponse::getSubtotal).sum());
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
}
