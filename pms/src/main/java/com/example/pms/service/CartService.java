package com.example.pms.service;

import com.example.pms.model.Cart;
import com.example.pms.model.User;

import java.math.BigDecimal;

public interface CartService {
    Cart getOrCreateCart(User customer);
    void addToCart(User customer, Long productId, int quantity);
    void updateQuantity(User customer, Long itemId, int quantity);
    void removeItem(User customer, Long itemId);
    void clearCart(User customer);
    BigDecimal getCartTotal(Cart cart);
}
