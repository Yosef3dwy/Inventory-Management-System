package com.example.inventory.service.impl;

import com.example.inventory.exception.CapacityExceededException;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.InvalidInputException;
import com.example.inventory.model.Inventory;
import com.example.inventory.model.Product;
import com.example.inventory.model.Warehouse;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.WarehouseRepository;
import com.example.inventory.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, WarehouseRepository warehouseRepository) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasStock(Product product, int quantity) {
        if (product == null) throw new InvalidInputException("Product cannot be null");
        if (quantity <= 0) throw new InvalidInputException("Quantity checked must be greater than zero.");

        List<Inventory> inventories = inventoryRepository.findByProduct(product);
        int totalStock = inventories.stream().mapToInt(Inventory::getQuantity).sum();

        return totalStock >= quantity;
    }

    

    @Override
    public void reserveStock(Product product, int quantity) {
        if (product == null) throw new InvalidInputException("Product cannot be null");
        if (quantity <= 0) throw new InvalidInputException("Quantity to reserve must be greater than zero.");

        List<Inventory> inventories = inventoryRepository.findByProduct(product);
        
        int remainingToDeduct = removeFromWarehouses(product, inventories, quantity);

        if (remainingToDeduct > 0) {
            throw new InsufficientStockException(
                    "Not enough stock for product ID: " + product.getProductId() +
                    ". Missing: " + remainingToDeduct
            );
        }
    }

    /**
     * Private helper function to handle the warehouse distribution logic.
     * Returns the remaining quantity that could not be removed (if any).
     */
    private int removeFromWarehouses(Product product, List<Inventory> inventories, int diffQuantity) {
        int productVolume = product.getSize();

        for (Inventory inv : inventories) {
            int storedQuantity = inv.getQuantity();
            
            if (storedQuantity == 0) continue;

            Warehouse wh = inv.getWarehouse(); 

            if (diffQuantity >= storedQuantity) {
                wh.setFreeSpace(wh.getFreeSpace() + (storedQuantity * productVolume));
                diffQuantity -= storedQuantity;
                inv.setQuantity(0);
            } else {
                wh.setFreeSpace(wh.getFreeSpace() + (diffQuantity * productVolume));
                inv.setQuantity(storedQuantity - diffQuantity);
                diffQuantity = 0; 
            }

            warehouseRepository.save(wh);
            inventoryRepository.save(inv);

            if (diffQuantity == 0) break;
        }
        
        return diffQuantity;
    }

    @Override
    public void restock(Product product, int quantity) {
        int leftover = distributeToWarehouses(product, quantity);
        
        if (leftover > 0) {
            throw new CapacityExceededException("Not enough warehouse space across all locations to store " + leftover + " items of Product ID: " + product.getProductId());
        }
    }

    @Override
    public int distributeToWarehouses(Product product, int quantity) {
        if (quantity <= 0) return quantity;

        int productVolume = product.getSize(); 
        int remainingQuantity = quantity;
        List<Warehouse> warehouses = warehouseRepository.findAll();

        for (Warehouse wh : warehouses) {
            if (remainingQuantity == 0) break;

            int freeSpace = wh.getFreeSpace();
            int maxItemsItCanHold = (productVolume > 0) ? (freeSpace / productVolume) : remainingQuantity;

            if (maxItemsItCanHold > 0) {
                int itemsToStore = Math.min(remainingQuantity, maxItemsItCanHold);

                wh.setFreeSpace(freeSpace - (itemsToStore * productVolume));
                warehouseRepository.save(wh);

                Inventory inventory = inventoryRepository.findByWarehouseAndProduct(wh, product)
                        .orElseGet(() -> {
                            Inventory newInv = new Inventory();
                            newInv.setWarehouse(wh);
                            newInv.setProduct(product);
                            newInv.setQuantity(0);
                            return newInv;
                        });

                inventory.setQuantity(inventory.getQuantity() + itemsToStore);
                inventoryRepository.save(inventory);

                remainingQuantity -= itemsToStore;
            }
        }

        return remainingQuantity; 
    }

    @Override
    public void addWarehouse(Warehouse warehouse) {
        if (warehouse.getTotalCapacity() <= 0) {
            throw new InvalidInputException("Warehouse total capacity must be greater than zero.");
        }

        warehouse.setFreeSpace(warehouse.getTotalCapacity());
        
        warehouseRepository.save(warehouse);
    }

    @Override
    public void clearWarehouse(Warehouse warehouse) {
        List<Inventory> inventories = inventoryRepository.findByWarehouse(warehouse);

        // 1. Temporarily zero out the free space so distributeToWarehouses ignores this warehouse
        warehouse.setFreeSpace(0);
        warehouseRepository.save(warehouse);

        // 2. Loop through existing stock and redistribute
        for (Inventory inv : inventories) {
            int quantityToMove = inv.getQuantity();
            Product product = inv.getProduct();

            // Delete the inventory record from the clearing warehouse first
            inventoryRepository.delete(inv);

            if (quantityToMove > 0) {
                // Delegate to the existing distribution logic
                int leftover = distributeToWarehouses(product, quantityToMove);
                
                // Rollback safety check: abort if the remaining warehouses are full
                if (leftover > 0) {
                    throw new CapacityExceededException(
                            "Cannot clear warehouse ID " + warehouse.getWarehouseId() + 
                            ". Not enough capacity in other locations to redistribute Product ID: " + 
                            product.getProductId() + ". Leftover items: " + leftover
                    );
                }
            }
        }

        // 3. Reset the warehouse capacity to fully empty once redistribution is complete
        warehouse.setFreeSpace(warehouse.getTotalCapacity());
        warehouseRepository.save(warehouse);
    }

    @Override
    public void removeWarehouse(Warehouse warehouse) {
        clearWarehouse(warehouse); 
        warehouseRepository.delete(warehouse);
    }
}