package com.example.pms.repository;

import com.example.pms.model.CustomerOrder;
import com.example.pms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByCustomerOrderByOrderDateDesc(User customer);
}
