package com.example.pms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
@Getter
@Setter
@NoArgsConstructor
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    // Non-sensitive display reference only — e.g. card's last 4 digits or a UPI ID.
    // Full card numbers / CVV are never accepted or stored (see CheckoutController).
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CONFIRMED;

    private LocalDateTime orderDate = LocalDateTime.now();

    // A customer can back out only while nothing has shipped yet: the order must
    // still be CONFIRMED, and no supplier on it has marked their line as received.
    public boolean isCancellable() {
        return status == OrderStatus.CONFIRMED && items.stream().noneMatch(OrderItem::isReceived);
    }
}
