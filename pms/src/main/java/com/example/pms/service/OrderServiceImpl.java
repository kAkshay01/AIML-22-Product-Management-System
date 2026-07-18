package com.example.pms.service;

import com.example.pms.model.*;
import com.example.pms.repository.CustomerOrderRepository;
import com.example.pms.repository.OrderItemRepository;
import com.example.pms.repository.ProductRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderServiceImpl implements OrderService {

    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;

    public OrderServiceImpl(CustomerOrderRepository orderRepository, ProductRepository productRepository,
                             OrderItemRepository orderItemRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
    }

    @Override
    @Transactional
    public CustomerOrder placeOrder(User customer, String shippingAddress, PaymentMethod paymentMethod,
                                     String paymentReference) {

        Cart cart = cartService.getOrCreateCart(customer);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Your cart is empty — add something before checking out.");
        }

        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentReference(paymentReference);

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            // Stock may have moved since the item was added to the cart — cap to what's
            // actually available rather than overselling.
            int quantity = Math.min(cartItem.getQuantity(), Math.max(product.getQuantity(), 0));
            if (quantity < 1) {
                continue;
            }

            OrderItem orderItem = new OrderItem(order, product, quantity);
            order.getItems().add(orderItem);
            total = total.add(orderItem.getLineTotal());

            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("None of the items in your cart are in stock anymore.");
        }

        order.setTotalAmount(total);
        CustomerOrder saved = orderRepository.save(order);

        cartService.clearCart(customer);

        return saved;
    }

    @Override
    public List<CustomerOrder> findOrdersForCustomer(User customer) {
        return orderRepository.findByCustomerOrderByOrderDateDesc(customer);
    }

    @Override
    public CustomerOrder findOwnedOrder(User customer, Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId));
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("This order does not belong to you.");
        }
        return order;
    }

    @Override
    @Transactional
    public void cancelOrder(User customer, Long orderId) {
        CustomerOrder order = findOwnedOrder(customer, orderId);

        if (!order.isCancellable()) {
            throw new IllegalStateException(order.getStatus() == OrderStatus.CANCELLED
                    ? "This order has already been cancelled."
                    : "This order can no longer be cancelled — a supplier has already started fulfilling it.");
        }

        // Return every line item's quantity to stock.
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    public List<OrderItem> findOrderItemsForSupplier(User supplier) {
        return orderItemRepository.findBySupplierOrderByOrderDateDesc(supplier);
    }

    @Override
    @Transactional
    public void markItemReceived(User supplier, Long itemId) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Order item not found with id: " + itemId));

        if (item.getProduct() == null || item.getProduct().getSupplier() == null
                || !item.getProduct().getSupplier().getId().equals(supplier.getId())) {
            throw new AccessDeniedException("You do not have permission to update this order item.");
        }
        if (item.getOrder().getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("This order was cancelled by the customer.");
        }

        item.setReceived(true);
        orderItemRepository.save(item);
    }
}
