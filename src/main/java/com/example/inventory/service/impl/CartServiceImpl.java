package com.example.inventory.service.impl;

import com.example.inventory.exception.InvalidInputException;
import com.example.inventory.model.Cart;
import com.example.inventory.model.CartItem;
import com.example.inventory.model.Customer;
import com.example.inventory.model.Product;
import com.example.inventory.repository.CartItemRepository;
import com.example.inventory.repository.CartRepository;
import com.example.inventory.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Cart getOrCreateCart(Customer customer) {
        if (customer == null) throw new InvalidInputException("Customer cannot be null");

        return cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public CartItem addItem(Customer customer, Product product, int quantity) {
        if (customer == null) throw new InvalidInputException("Customer cannot be null");
        if (product == null) throw new InvalidInputException("Product cannot be null");
        if (quantity <= 0) throw new InvalidInputException("Quantity must be greater than zero");

        Cart cart = getOrCreateCart(customer);
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem itemToSave;
        if (existingItem.isPresent()) {
            itemToSave = existingItem.get();
            itemToSave.setQuantity(itemToSave.getQuantity() + quantity);
        } else {
            itemToSave = new CartItem();
            itemToSave.setCart(cart);
            itemToSave.setProduct(product);
            itemToSave.setQuantity(quantity);
            cart.getItems().add(itemToSave);
        }

        return cartItemRepository.save(itemToSave);
    }

    @Override
    public void updateItemQuantity(Customer customer, Product product, int newQuantity) {
        if (customer == null) throw new InvalidInputException("Customer cannot be null");
        if (product == null) throw new InvalidInputException("Product cannot be null");

        if (newQuantity <= 0) {
            removeItem(customer, product);
            return;
        }

        Cart cart = getOrCreateCart(customer);
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new InvalidInputException("Product not found in customer cart"));

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
    }

    @Override
    public void removeItem(Customer customer, Product product) {
        if (customer == null) throw new InvalidInputException("Customer cannot be null");
        if (product == null) throw new InvalidInputException("Product cannot be null");

        Cart cart = getOrCreateCart(customer);
        cartItemRepository.findByCartAndProduct(cart, product)
                .ifPresent(cartItem -> {
                    cart.getItems().remove(cartItem);
                    cartItemRepository.delete(cartItem);
                });
    }

    @Override
    public void clearCart(Customer customer) {
        if (customer == null) throw new InvalidInputException("Customer cannot be null");

        Cart cart = getOrCreateCart(customer);
        cartItemRepository.deleteByCart(cart);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}