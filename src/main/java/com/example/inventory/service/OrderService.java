package com.example.inventory.service;

import com.example.inventory.model.Customer;
import com.example.inventory.model.Order;

import java.util.Date;
import java.util.List;

public interface OrderService {
    Order checkout(Customer customer);

    List<Order> getOrderHistory(Customer customer);

    Order updateStatus(Long orderId, String newStatus, Date deliveredDate);
}