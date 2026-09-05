package com.styletry.backend.service;

import com.styletry.backend.dto.request.CartRequest;
import com.styletry.backend.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(String email);
    CartResponse addToCart(String email, CartRequest request);
    CartResponse updateCartItem(String email, Long itemId, Integer quantity);
    void removeCartItem(String email, Long itemId);
    void clearCart(String email);
}
