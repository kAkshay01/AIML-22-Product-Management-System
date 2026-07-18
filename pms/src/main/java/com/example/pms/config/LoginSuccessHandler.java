package com.example.pms.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {

        boolean isSupplier = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPLIER"));
        boolean isCustomer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        // The login form lets the person pick "Log in as Customer/Supplier". If that
        // doesn't match the account's actual role, bounce them back rather than let
        // them into a dashboard meant for the other role.
        String loginRole = request.getParameter("loginRole");
        if (loginRole != null) {
            boolean matches = (loginRole.equalsIgnoreCase("SUPPLIER") && isSupplier)
                    || (loginRole.equalsIgnoreCase("CUSTOMER") && isCustomer);
            if (!matches) {
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/login?roleMismatch");
                return;
            }
        }

        String target = isSupplier
                ? request.getContextPath() + "/supplier/dashboard"
                : request.getContextPath() + "/customer/dashboard";
        response.sendRedirect(target);
    }
}
