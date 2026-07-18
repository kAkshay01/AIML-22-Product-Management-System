package com.example.pms.controller;

import com.example.pms.model.Cart;
import com.example.pms.model.CustomerOrder;
import com.example.pms.model.PaymentMethod;
import com.example.pms.model.User;
import com.example.pms.repository.UserRepository;
import com.example.pms.service.CartService;
import com.example.pms.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    public CheckoutController(CartService cartService, OrderService orderService, UserRepository userRepository) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication) {
        User user = currentUser(authentication);
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            return "redirect:/customer/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", cartService.getCartTotal(cart));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "checkout";
    }

    @PostMapping("/checkout/place")
    public String placeOrder(@RequestParam String shippingAddress,
                              @RequestParam PaymentMethod paymentMethod,
                              @RequestParam(required = false) String cardNumber,
                              @RequestParam(required = false) String upiId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        User user = currentUser(authentication);

        // Only a non-sensitive display reference is ever kept. The full card number
        // (if entered) is used just long enough to read its last 4 digits and is then
        // discarded — it is never persisted, and this app never collects a CVV at all.
        // This is a simulated checkout, not a real payment integration.
        String paymentReference = switch (paymentMethod) {
            case CARD -> {
                String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
                yield digits.length() >= 4
                        ? "Card ending in " + digits.substring(digits.length() - 4)
                        : "Card payment";
            }
            case UPI -> (upiId == null || upiId.isBlank()) ? "UPI payment" : upiId.trim();
            case CASH_ON_DELIVERY -> null;
        };

        try {
            CustomerOrder order = orderService.placeOrder(user, shippingAddress, paymentMethod, paymentReference);
            redirectAttributes.addFlashAttribute("placed", true);
            return "redirect:/customer/orders/" + order.getId();
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customer/cart";
        }
    }

    @GetMapping("/orders")
    public String orderHistory(Model model, Authentication authentication) {
        User user = currentUser(authentication);
        model.addAttribute("orders", orderService.findOrdersForCustomer(user));
        return "order-history";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, Authentication authentication) {
        User user = currentUser(authentication);
        CustomerOrder order = orderService.findOwnedOrder(user, id);
        model.addAttribute("order", order);
        return "order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        User user = currentUser(authentication);
        try {
            orderService.cancelOrder(user, id);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled and items returned to stock.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/customer/orders/" + id;
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user not found: " + authentication.getName()));
    }
}
