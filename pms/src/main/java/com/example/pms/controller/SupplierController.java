package com.example.pms.controller;

import com.example.pms.model.Product;
import com.example.pms.model.User;
import com.example.pms.repository.UserRepository;
import com.example.pms.service.OrderService;
import com.example.pms.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/supplier")
public class SupplierController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    public SupplierController(ProductService productService, OrderService orderService,
                               UserRepository userRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String successMessage,
                             Model model, Authentication authentication) {

        User supplier = currentUser(authentication);
        List<Product> products = (keyword != null && !keyword.isBlank())
                ? productService.searchBySupplier(supplier, keyword)
                : productService.findBySupplier(supplier);

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("username", supplier.getFullName());
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        return "supplier-dashboard";
    }

    @GetMapping("/orders")
    public String orders(Model model, Authentication authentication) {
        User supplier = currentUser(authentication);
        model.addAttribute("orderItems", orderService.findOrderItemsForSupplier(supplier));
        model.addAttribute("username", supplier.getFullName());
        return "supplier-orders";
    }

    @PostMapping("/orders/receive/{itemId}")
    public String receiveOrder(@PathVariable Long itemId, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            orderService.markItemReceived(currentUser(authentication), itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Order item marked as received.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/supplier/orders";
    }

    @GetMapping("/products/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("formTitle", "Add Product");
        model.addAttribute("formAction", "/supplier/products/save");
        return "supplier-add-product";
    }

    @PostMapping("/products/save")
    public String save(@Valid @ModelAttribute("product") Product product, BindingResult result,
                        Model model, Authentication authentication, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Add Product");
            model.addAttribute("formAction", "/supplier/products/save");
            return "supplier-add-product";
        }

        product.setSupplier(currentUser(authentication));
        productService.save(product);
        redirectAttributes.addAttribute("successMessage", "Product added successfully!");
        return "redirect:/supplier/dashboard";
    }

    @GetMapping("/products/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Authentication authentication) {
        Product product = requireOwnedProduct(id, authentication);
        model.addAttribute("product", product);
        model.addAttribute("formTitle", "Edit Product");
        model.addAttribute("formAction", "/supplier/products/update/" + id);
        return "supplier-edit-product";
    }

    @PostMapping("/products/update/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("product") Product product, BindingResult result,
                          Model model, Authentication authentication, RedirectAttributes redirectAttributes) {

        Product existing = requireOwnedProduct(id, authentication);

        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Edit Product");
            model.addAttribute("formAction", "/supplier/products/update/" + id);
            return "supplier-edit-product";
        }

        product.setId(id);
        product.setSupplier(existing.getSupplier());
        productService.save(product);
        redirectAttributes.addAttribute("successMessage", "Product updated successfully!");
        return "redirect:/supplier/dashboard";
    }

    @GetMapping("/products/delete/{id}")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        requireOwnedProduct(id, authentication);
        productService.deleteById(id);
        redirectAttributes.addAttribute("successMessage", "Product deleted successfully!");
        return "redirect:/supplier/dashboard";
    }

    private Product requireOwnedProduct(Long id, Authentication authentication) {
        Product product = productService.findById(id);
        User supplier = currentUser(authentication);
        if (product.getSupplier() == null || !product.getSupplier().getId().equals(supplier.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this product.");
        }
        return product;
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found: " + authentication.getName()));
    }
}
