package com.example.pms.service;

import com.example.pms.model.Cart;
import com.example.pms.model.CartItem;
import com.example.pms.model.Product;
import com.example.pms.model.User;
import com.example.pms.repository.CartItemRepository;
import com.example.pms.repository.CartRepository;
import com.example.pms.repository.ProductRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
                            ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Cart getOrCreateCart(User customer) {
        return cartRepository.findByCustomer(customer)
                .orElseGet(() -> cartRepository.save(new Cart(customer)));
    }

    @Override
    @Transactional
    public void addToCart(User customer, Long productId, int quantity) {
        if (quantity < 1) {
            quantity = 1;
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + productId));

        Cart cart = getOrCreateCart(customer);

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQuantity = Math.min(item.getQuantity() + quantity, Math.max(product.getQuantity(), 0));
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            int cappedQuantity = Math.min(quantity, Math.max(product.getQuantity(), 0));
            if (cappedQuantity < 1) {
                cappedQuantity = 1;
            }
            CartItem item = new CartItem(cart, product, cappedQuantity);
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }
    }

    @Override
    @Transactional
    public void updateQuantity(User customer, Long itemId, int quantity) {
        CartItem item = requireOwnedItem(customer, itemId);
        if (quantity <= 0) {
            removeItem(customer, itemId);
            return;
        }
        int cappedQuantity = Math.min(quantity, Math.max(item.getProduct().getQuantity(), 0));
        item.setQuantity(Math.max(cappedQuantity, 1));
        cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeItem(User customer, Long itemId) {
        CartItem item = requireOwnedItem(customer, itemId);
        item.getCart().getItems().remove(item);
        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void clearCart(User customer) {
        Cart cart = getOrCreateCart(customer);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public BigDecimal getCartTotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartItem requireOwnedItem(User customer, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Cart item not found with id: " + itemId));
        if (!item.getCart().getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("This cart item does not belong to you.");
        }
        return item;
    }
}
