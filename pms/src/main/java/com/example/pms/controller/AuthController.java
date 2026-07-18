package com.example.pms.controller;

import com.example.pms.model.Role;
import com.example.pms.model.User;
import com.example.pms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user, BindingResult result,
                            Model model, RedirectAttributes redirectAttributes) {

        if (user.getUsername() != null && userService.usernameExists(user.getUsername())) {
            result.rejectValue("username", "duplicate", "That username is already taken");
        }
        if (user.getEmail() != null && userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "duplicate", "That email is already registered");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "register";
        }

        userService.registerUser(user);
        redirectAttributes.addAttribute("registered", true);
        return "redirect:/login";
    }
}
