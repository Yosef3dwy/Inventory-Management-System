package com.example.inventory.repository;

import com.example.inventory.model.Order;
import com.example.inventory.model.OrderItem;
import com.example.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByProductIn(List<Product> products);

    List<OrderItem> findByProduct(Product product);

    void deleteByOrder(Order order);
}
