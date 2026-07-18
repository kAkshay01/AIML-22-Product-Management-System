package com.example.pms.service;

import com.example.pms.model.CustomerOrder;
import com.example.pms.model.OrderItem;
import com.example.pms.model.PaymentMethod;
import com.example.pms.model.User;

import java.util.List;

public interface OrderService {
    CustomerOrder placeOrder(User customer, String shippingAddress, PaymentMethod paymentMethod, String paymentReference);
    List<CustomerOrder> findOrdersForCustomer(User customer);
    CustomerOrder findOwnedOrder(User customer, Long orderId);

    // Customer-initiated cancellation. Restocks every line item and only
    // succeeds while the order is still cancellable (see CustomerOrder).
    void cancelOrder(User customer, Long orderId);

    // Supplier-facing order queue: every line item across all orders that
    // includes one of this supplier's products.
    List<OrderItem> findOrderItemsForSupplier(User supplier);

    // Supplier marks their own line item as received/pulled for fulfilment.
    void markItemReceived(User supplier, Long itemId);
}
