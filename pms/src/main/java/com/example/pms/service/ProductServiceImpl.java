package com.example.pms.service;

import com.example.pms.model.Product;
import com.example.pms.model.User;
import com.example.pms.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public List<Product> search(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(keyword, keyword);
    }

    @Override
    public Page<Product> search(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Override
    public List<Product> findBySupplier(User supplier) {
        return productRepository.findBySupplier(supplier);
    }

    @Override
    public List<Product> searchBySupplier(User supplier, String keyword) {
        return productRepository.searchBySupplier(supplier, keyword);
    }

    @Override
    public List<String> findAllCategories() {
        return productRepository.findDistinctCategories();
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
