package com.example.inventory.security;

import com.example.inventory.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(account.getEmail())
                .password(account.getPassword())
                .authorities("ROLE_" + account.getUserRole().name()) // dynamically sets ROLE_CUSTOMER, ROLE_ADMIN, etc.
                .build();
    }
}