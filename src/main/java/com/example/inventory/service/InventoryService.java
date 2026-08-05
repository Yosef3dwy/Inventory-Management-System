package com.example.inventory.service;

import com.example.inventory.model.Product;

public interface InventoryService {
    boolean hasStock(Product product, int quantity);

    void reserveStock(Product product, int quantity);   // decrement

    void restock(Product product, int quantity);         // increment, used by SupplyService
}
