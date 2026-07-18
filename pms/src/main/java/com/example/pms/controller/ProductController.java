package com.example.pms.controller;

import com.example.pms.model.Product;
import com.example.pms.model.User;
import com.example.pms.repository.UserRepository;
import com.example.pms.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, UserRepository userRepository) {
        this.productService = productService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(defaultValue = "id") String sortField,
                        @RequestParam(defaultValue = "asc") String sortDir,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        @RequestParam(required = false) String successMessage,
                        Model model) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = (keyword != null && !keyword.isBlank())
                ? productService.search(keyword, pageable)
                : productService.findAll(pageable);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equalsIgnoreCase("asc") ? "desc" : "asc");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", size);
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        return "product-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("formTitle", "Add Product");
        model.addAttribute("formAction", "/products/save");
        model.addAttribute("categories", productService.findAllCategories());
        return "add-product";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("product") Product product, BindingResult result,
                        Model model, Authentication authentication, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Add Product");
            model.addAttribute("formAction", "/products/save");
            model.addAttribute("categories", productService.findAllCategories());
            return "add-product";
        }

        product.setSupplier(currentUser(authentication));
        productService.save(product);
        redirectAttributes.addAttribute("successMessage", "Product saved successfully!");
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("formTitle", "Edit Product");
        model.addAttribute("formAction", "/products/update/" + id);
        model.addAttribute("categories", productService.findAllCategories());
        return "edit-product";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("product") Product product, BindingResult result,
                          Model model, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Edit Product");
            model.addAttribute("formAction", "/products/update/" + id);
            model.addAttribute("categories", productService.findAllCategories());
            return "edit-product";
        }

        Product existing = productService.findById(id);
        product.setId(id);
        product.setSupplier(existing.getSupplier());
        productService.save(product);
        redirectAttributes.addAttribute("successMessage", "Product updated successfully!");
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addAttribute("successMessage", "Product deleted successfully!");
        return "redirect:/products";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found: " + authentication.getName()));
    }
}
