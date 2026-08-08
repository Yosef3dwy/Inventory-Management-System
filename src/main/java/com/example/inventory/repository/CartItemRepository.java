package com.example.inventory.repository;

import com.example.inventory.model.Cart;
import com.example.inventory.model.CartItem;
import com.example.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // List everything all products in a cart
    List<CartItem> findByCart(Cart cart);

    // Check if a product's already in the cart before inserting a duplicate row
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    // Clear cart on checkout
    void deleteByCart(Cart cart);
}
