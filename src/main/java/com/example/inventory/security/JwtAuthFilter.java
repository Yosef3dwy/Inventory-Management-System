package com.example.inventory.security;

import com.example.inventory.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class JwtAuthFilter implements HandlerInterceptor {
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!path.startsWith("/api/") || path.equals("/api/auth/login")) {
            return true;
        }

        AuthUser user = userDetailsService.findByToken(readBearerToken(request));
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login is required.");
            return false;
        }

        request.setAttribute("authUser", user);

        if (!isAllowed(user.role(), method, path, request)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Your role cannot perform this action.");
            return false;
        }

        return true;
    }

    private boolean isAllowed(UserRole role, String method, String path, HttpServletRequest request) {
        if (role == UserRole.ADMIN) {
            return true;
        }

        if (role == UserRole.CUSTOMER) {
            return customerAllowed(method, path, request);
        }

        if (role == UserRole.SUPPLIER) {
            return supplierAllowed(method, path, request);
        }

        return false;
    }

    private boolean customerAllowed(String method, String path, HttpServletRequest request) {
        AuthUser user = (AuthUser) request.getAttribute("authUser");
        if (HttpMethod.GET.matches(method) && path.equals("/api/products")) return true;
        if (HttpMethod.GET.matches(method) && path.matches("/api/products/\\d+")) return true;
        if (path.startsWith("/api/carts/" + user.userId())) return true;
        if (HttpMethod.POST.matches(method) && path.equals("/api/orders/checkout/" + user.userId())) return true;
        if (HttpMethod.PATCH.matches(method) && path.matches("/api/orders/\\d+/cancel")) return true;
        return HttpMethod.GET.matches(method) && path.equals("/api/orders/customer/" + user.userId());
    }

    private boolean supplierAllowed(String method, String path, HttpServletRequest request) {
        AuthUser user = (AuthUser) request.getAttribute("authUser");
        if (HttpMethod.GET.matches(method) && path.equals("/api/products")) return true;
        if (HttpMethod.GET.matches(method) && path.matches("/api/products/\\d+")) return true;
        if (HttpMethod.GET.matches(method) && path.equals("/api/suppliers/" + user.userId())) return true;
        if (HttpMethod.GET.matches(method) && path.equals("/api/suppliers/" + user.userId() + "/supplies")) return true;
        if (HttpMethod.GET.matches(method) && path.equals("/api/suppliers/" + user.userId() + "/sales")) return true;
        if (HttpMethod.PUT.matches(method) && path.equals("/api/suppliers/" + user.userId())) return true;
        if (HttpMethod.PUT.matches(method) && path.matches("/api/suppliers/" + user.userId() + "/products/\\d+")) return true;
        if (HttpMethod.DELETE.matches(method) && path.matches("/api/suppliers/" + user.userId() + "/products/\\d+")) return true;
        return HttpMethod.POST.matches(method) && path.equals("/api/suppliers/" + user.userId() + "/products");
    }

    private String readBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return "";
        }
        return header.substring(7);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
