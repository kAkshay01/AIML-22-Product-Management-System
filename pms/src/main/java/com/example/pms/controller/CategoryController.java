package com.example.pms.controller;

import com.example.pms.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CategoryController {

    private final ProductService productService;

    public CategoryController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", productService.findAllCategories());
        return "categories";
    }
}
