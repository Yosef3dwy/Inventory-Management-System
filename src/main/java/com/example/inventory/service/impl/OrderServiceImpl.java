package com.example.inventory.service.impl;

import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.model.*;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.service.CartService;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            CartService cartService,
                            InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
    }

    @Override
    public Order checkout(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        // 1. Load the customer's cart and check if empty
        Cart cart = cartService.getOrCreateCart(customer);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout with an empty cart for customer ID: " + customer.getCustomerId());
        }

        // 2. Validate first (check stock for all items before mutating state)
        for (CartItem cartItem : cart.getItems()) {
            if (!inventoryService.hasStock(cartItem.getProduct(), cartItem.getQuantity())) {
                // 3. Throw immediately if any item fails stock check (no stock touched, no order created)
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + cartItem.getProduct().getProductId() +
                                " (Requested: " + cartItem.getQuantity() + ")"
                );
            }
        }

        // 4. Create a new Order (status PENDING, OrderDate = now)
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus("PENDING");
        order.setOrderDate(new Date());

        // 5. Build OrderItems and snapshot unit price at time of purchase
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());

            // Price snapshot: preserve purchase price even if product price changes later
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());

            order.getItems().add(orderItem);
        }

        // Save order and items
        Order savedOrder = orderRepository.save(order);

        // 6. Mutate stock (reserve stock now that all items are validated)
        for (CartItem cartItem : cart.getItems()) {
            inventoryService.reserveStock(cartItem.getProduct(), cartItem.getQuantity());
        }

        // 7. Clear the cart
        cartService.clearCart(customer);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    @Override
    public Order updateStatus(Long orderId, String newStatus) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (newStatus == null || newStatus.isBlank()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        order.setStatus(newStatus.toUpperCase());
        return orderRepository.save(order);
    }
}