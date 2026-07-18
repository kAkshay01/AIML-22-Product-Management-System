package com.example.pms.controller;

import com.example.pms.model.Cart;
import com.example.pms.model.Product;
import com.example.pms.model.User;
import com.example.pms.repository.UserRepository;
import com.example.pms.service.CartService;
import com.example.pms.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final ProductService productService;
    private final CartService cartService;
    private final UserRepository userRepository;

    public CustomerController(ProductService productService, CartService cartService, UserRepository userRepository) {
        this.productService = productService;
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String successMessage,
                             Model model, Authentication authentication) {

        User user = currentUser(authentication);
        List<Product> products = (keyword != null && !keyword.isBlank())
                ? productService.search(keyword)
                : productService.findAll();

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("username", user.getFullName());
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        return "customer-dashboard";
    }

    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id,
                             @RequestParam(defaultValue = "1") int quantity,
                             @RequestParam(required = false) String keyword,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        cartService.addToCart(currentUser(authentication), id, quantity);
        redirectAttributes.addAttribute("successMessage", "Added to cart!");
        if (keyword != null && !keyword.isBlank()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }
        return "redirect:/customer/dashboard";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, Authentication authentication) {
        User user = currentUser(authentication);
        Cart cart = cartService.getOrCreateCart(user);
        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", cartService.getCartTotal(cart));
        return "cart";
    }

    @PostMapping("/cart/update/{itemId}")
    public String updateCartItem(@PathVariable Long itemId, @RequestParam int quantity, Authentication authentication) {
        cartService.updateQuantity(currentUser(authentication), itemId, quantity);
        return "redirect:/customer/cart";
    }

    @PostMapping("/cart/remove/{itemId}")
    public String removeCartItem(@PathVariable Long itemId, Authentication authentication) {
        cartService.removeItem(currentUser(authentication), itemId);
        return "redirect:/customer/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(Authentication authentication) {
        cartService.clearCart(currentUser(authentication));
        return "redirect:/customer/cart";
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found: " + authentication.getName()));
    }
}
