package com.example.inventory.service.impl;

import com.example.inventory.exception.CartEmptyException;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.InvalidInputException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.*;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.service.CartService;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.OrderService;
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

    public OrderServiceImpl(OrderRepository orderRepository, CartService cartService, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order checkout(Customer customer) {
        if (customer == null) throw new InvalidInputException("Customer cannot be null");

        Cart cart = cartService.getOrCreateCart(customer);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartEmptyException("Cannot checkout with an empty cart for customer ID: " + customer.getCustomerId());
        }

        for (CartItem cartItem : cart.getItems()) {
            if (!inventoryService.hasStock(cartItem.getProduct(), cartItem.getQuantity())) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + cartItem.getProduct().getProductId() +
                                " (Requested: " + cartItem.getQuantity() + ")"
                );
            }
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus("PENDING");
        order.setOrderDate(new Date());

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());

            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Deducts stock across all warehouses
        for (CartItem cartItem : cart.getItems()) {
            inventoryService.reserveStock(cartItem.getProduct(), cartItem.getQuantity());
        }

        cartService.clearCart(customer);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrderHistory(Customer customer) {
        if (customer == null) throw new InvalidInputException("Customer cannot be null");

        return orderRepository.findByCustomerOrderByOrderDateDesc(customer);
    }

    @Override
    public Order updateStatus(Long orderId, String newStatus, Date deliveredDate) {
        if (orderId == null) throw new InvalidInputException("Order ID cannot be null");
        if (newStatus == null || newStatus.isBlank()) throw new InvalidInputException("Status cannot be empty");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setDeliveredDate(deliveredDate);
        order.setStatus(newStatus.toUpperCase());
        return orderRepository.save(order);
    }
}