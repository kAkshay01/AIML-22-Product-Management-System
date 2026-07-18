package com.example.pms.repository;

import com.example.pms.model.Product;
import com.example.pms.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Paged catalog search (generic /products page)
    Page<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String category, Pageable pageable);

    // Unpaged catalog search (customer dashboard)
    List<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String category);

    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findDistinctCategories();

    List<Product> findBySupplier(User supplier);

    @Query("SELECT p FROM Product p WHERE p.supplier = :supplier " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchBySupplier(@Param("supplier") User supplier, @Param("keyword") String keyword);
}
