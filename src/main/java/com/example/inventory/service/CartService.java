package com.example.inventory.service;

import com.example.inventory.model.Cart;
import com.example.inventory.model.CartItem;
import com.example.inventory.model.Customer;
import com.example.inventory.model.Product;

public interface CartService {
    Cart getOrCreateCart(Customer customer);

    CartItem addItem(Customer customer, Product product, int quantity);

    void updateItemQuantity(Customer customer, Product product, int newQuantity);

    void removeItem(Customer customer, Product product);

    void clearCart(Customer customer);
}
