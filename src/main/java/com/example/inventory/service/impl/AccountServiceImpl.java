package com.example.inventory.service.impl;

import com.example.inventory.enums.UserRole;
import com.example.inventory.exception.InvalidInputException;
import com.example.inventory.model.Account;
import com.example.inventory.repository.AccountRepository;
import com.example.inventory.service.AccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    // private final PasswordEncoder passwordEncoder; // Add this when you setup Spring Security

    public AccountServiceImpl(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Account createAccount(String email, String password, UserRole role) {
        if (email == null || email.isBlank()) {
            throw new InvalidInputException("Email cannot be empty");
        }
        if (password == null || password.length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters long");
        }
        if (accountRepository.existsByEmail(email)) {
            throw new InvalidInputException("Email is already in use");
        }

        Account account = new Account();
        account.setEmail(email);
        
        account.setPassword(passwordEncoder.encode(password)); // Use this later
        
        account.setUserRole(role);

        return accountRepository.save(account);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public void updatePassword(Long accountId, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new InvalidInputException("New password must be at least 6 characters long");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new InvalidInputException("Account not found"));

        account.setPassword(passwordEncoder.encode(newPassword));
        
        accountRepository.save(account);
    }
}