package com.example.inventory.service.impl;

import com.example.inventory.model.Product;
import com.example.inventory.model.Supplier;
import com.example.inventory.model.Supply;
import com.example.inventory.repository.SupplyRepository;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.SupplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class SupplyServiceImpl implements SupplyService {

    private final SupplyRepository supplyRepository;
    private final InventoryService inventoryService;

    @Autowired
    public SupplyServiceImpl(SupplyRepository supplyRepository, InventoryService inventoryService) {
        this.supplyRepository = supplyRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public Supply recordSupply(Supplier supplier, Product product, int quantity, double cost) {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Supply quantity must be greater than zero");
        }
        if (cost < 0) {
            throw new IllegalArgumentException("Supply cost cannot be negative");
        }

        // 1. Create and populate the Supply audit record
        Supply supply = new Supply();
        supply.setSupplier(supplier);
        supply.setProduct(product);
        supply.setQuantity(quantity);
        supply.setCost(cost);
        supply.setSupplyDate(new Date());

        // Save audit record to DB
        Supply savedSupply = supplyRepository.save(supply);

        // 2. Increment stock in the default warehouse via InventoryService
        inventoryService.restock(product, quantity);

        return savedSupply;
    }
}