package com.example.inventory.security;

import com.example.inventory.dto.request.LoginRequestDTO;
import com.example.inventory.dto.response.LoginResponseDTO;
import com.example.inventory.enums.UserRole;
import com.example.inventory.exception.InvalidInputException;
import com.example.inventory.model.AdminUser;
import com.example.inventory.model.Customer;
import com.example.inventory.model.Supplier;
import com.example.inventory.repository.AdminUserRepository;
import com.example.inventory.repository.CustomerRepository;
import com.example.inventory.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserDetailsServiceImpl {
    private final AdminUserRepository adminUserRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final JwtUtil jwtUtil;
    private final Map<String, AuthUser> sessions = new ConcurrentHashMap<>();

    public UserDetailsServiceImpl(AdminUserRepository adminUserRepository,
                                  CustomerRepository customerRepository,
                                  SupplierRepository supplierRepository,
                                  JwtUtil jwtUtil) {
        this.adminUserRepository = adminUserRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (email.isBlank() || password.isBlank()) {
            throw new InvalidInputException("Email and password are required.");
        }

        Optional<AdminUser> admin = adminUserRepository.findByEmail(email);
        if (admin.isPresent() && passwordMatches(password, admin.get().getPassword())) {
            return startSession(UserRole.ADMIN, admin.get().getAdminUserId(), admin.get().getName(), admin.get().getEmail());
        }

        Optional<Supplier> supplier = supplierRepository.findByEmail(email);
        if (supplier.isPresent() && passwordMatches(password, supplier.get().getPassword())) {
            return startSession(UserRole.SUPPLIER, supplier.get().getSupplierId(), supplier.get().getName(), supplier.get().getEmail());
        }

        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent() && passwordMatches(password, customer.get().getPassword())) {
            return startSession(UserRole.CUSTOMER, customer.get().getCustomerId(), customer.get().getName(), customer.get().getEmail());
        }

        throw new InvalidInputException("Invalid email or password.");
    }

    public AuthUser findByToken(String token) {
        return sessions.get(token);
    }

    private LoginResponseDTO startSession(UserRole role, Long userId, String name, String email) {
        String token = jwtUtil.createToken();
        AuthUser user = new AuthUser(token, role, userId, name, email);
        sessions.put(token, user);
        return new LoginResponseDTO(token, role, userId, name, email);
    }

    private boolean passwordMatches(String submitted, String stored) {
        return stored != null && stored.equals(submitted);
    }
}
