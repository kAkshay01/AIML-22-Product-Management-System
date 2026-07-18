package com.example.pms.repository;

import com.example.pms.model.OrderItem;
import com.example.pms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.supplier = :supplier " +
           "ORDER BY oi.order.orderDate DESC")
    List<OrderItem> findBySupplierOrderByOrderDateDesc(@Param("supplier") User supplier);
}
