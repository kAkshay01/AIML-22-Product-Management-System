package com.example.pms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    // Kept for reference/navigation, but name/price are snapshotted below so this
    // order line stays accurate even if the product is later edited or removed.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String productName;
    private BigDecimal unitPrice;
    private int quantity;

    // Set by the supplier once they've pulled this line item for fulfilment.
    // Tracked per item (not on the whole order) since a single order can span
    // products from several different suppliers.
    private boolean received = false;

    public OrderItem(CustomerOrder order, Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.productName = product.getName();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
