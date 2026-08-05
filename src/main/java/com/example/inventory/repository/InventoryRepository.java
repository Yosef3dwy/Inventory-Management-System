package com.example.inventory.repository;

import com.example.inventory.model.Inventory;
import com.example.inventory.model.Product;
import com.example.inventory.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    // How much of this product exists across the warehouse
    List<Inventory> findByProduct(Product product);

    // Everything stored in one warehouse
    List<Inventory> findByWarehouse(Warehouse warehouse);

    // Check if a (warehouse, product) exists before inserting a duplicate
    Optional<Inventory> findByWarehouseAndProduct(Warehouse warehouse, Product product);
}
