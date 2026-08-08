package com.example.inventory.service;


import com.example.inventory.model.Product;
import com.example.inventory.model.Warehouse;

public interface InventoryService {
    boolean hasStock(Product product, int quantity);

    void reserveStock(Product product, int quantity);   // decrement

    void restock(Product product, int quantity);         // increment, used by SupplyService

    void addWarehouse(Warehouse warehouse);
    
    void clearWarehouse(Warehouse warehouse);

    void removeWarehouse(Warehouse warehouse);

    int distributeToWarehouses(Product product, int quantity);   // distribute products into all warehouses depending on the size of the product "returns remaining quantity that can't be distributed"

}