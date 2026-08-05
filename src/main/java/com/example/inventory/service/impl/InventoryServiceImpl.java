package com.example.inventory.service.impl;

import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.model.Inventory;
import com.example.inventory.model.Product;
import com.example.inventory.model.Warehouse;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.WarehouseRepository;
import com.example.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// Add warehouse id = 1 in the application.properties file

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final Long defaultWarehouseId;

    @Autowired
    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            WarehouseRepository warehouseRepository,
            @Value("${inventory.default-warehouse-id}") Long defaultWarehouseId) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.defaultWarehouseId = defaultWarehouseId;
    }

    private Warehouse getDefaultWarehouse() {
        return warehouseRepository.findById(defaultWarehouseId)
                .orElseThrow(() -> new IllegalStateException("Default warehouse not found for ID: " + defaultWarehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStock(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity checked must be greater than zero.");
        }

        Warehouse defaultWarehouse = getDefaultWarehouse();
        Optional<Inventory> inventory = inventoryRepository.findByWarehouseAndProduct(defaultWarehouse, product);

        // Treats "no row found" as zero stock (quantity >= requested evaluates to false)
        return inventory.map(inv -> inv.getQuantity() >= quantity).orElse(false);
    }

    @Override
    public void reserveStock(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to reserve must be greater than zero.");
        }

        Warehouse defaultWarehouse = getDefaultWarehouse();

        // 1. Fetch inventory row for default warehouse
        Inventory inventory = inventoryRepository.findByWarehouseAndProduct(defaultWarehouse, product)
                .orElseThrow(() -> new InsufficientStockException("No inventory record found for product ID: " + product.getProductId()));

        // 2. Check if subtraction would go negative
        if (inventory.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Not enough stock for product ID: " + product.getProductId() +
                            ". Requested: " + quantity + ", Available: " + inventory.getQuantity()
            );
        }

        // 3. Subtract quantity and save
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public void restock(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to restock must be greater than zero.");
        }

        Warehouse defaultWarehouse = getDefaultWarehouse();
        Inventory inventory = inventoryRepository.findByWarehouseAndProduct(defaultWarehouse, product)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setWarehouse(defaultWarehouse);
                    newInventory.setProduct(product);
                    newInventory.setQuantity(0);
                    return newInventory;
                });

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
    }
}