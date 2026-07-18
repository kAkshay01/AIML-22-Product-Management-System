package com.example.pms.service;

import com.example.pms.model.Product;
import com.example.pms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<Product> findAll();
    Page<Product> findAll(Pageable pageable);
    List<Product> search(String keyword);
    Page<Product> search(String keyword, Pageable pageable);
    List<Product> findBySupplier(User supplier);
    List<Product> searchBySupplier(User supplier, String keyword);
    List<String> findAllCategories();
    Product findById(Long id);
    Product save(Product product);
    void deleteById(Long id);
}
