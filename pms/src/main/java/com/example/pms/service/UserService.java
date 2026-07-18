package com.example.pms.service;

import com.example.pms.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User registerUser(User user);
    boolean usernameExists(String username);
    boolean emailExists(String email);
}
