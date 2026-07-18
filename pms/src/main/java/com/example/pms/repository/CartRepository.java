package com.example.pms.repository;

import com.example.pms.model.Cart;
import com.example.pms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(User customer);
}
