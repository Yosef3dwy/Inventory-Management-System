package com.example.inventory.service;

import com.example.inventory.enums.UserRole;
import com.example.inventory.model.Account;

import java.util.Optional;

public interface AccountService {
    Account createAccount(String email, String password, UserRole role);
    
    Optional<Account> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    void updatePassword(Long accountId, String newPassword);
}