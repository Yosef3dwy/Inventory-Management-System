package com.example.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.inventory.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
  
    boolean existsByEmail(String email);
  
    Optional<Account> findByEmail(String email);
}
