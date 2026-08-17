package com.example.inventory.repository;

import com.example.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE LOWER(p.Title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByTitleContainingIgnoreCase(String keyword);

    @Query("SELECT p FROM Product p WHERE p.Title = :title")
    Optional<Product> findByTitle(String title);
}
