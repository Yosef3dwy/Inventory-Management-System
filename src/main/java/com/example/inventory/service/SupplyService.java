package com.example.inventory.service;

import com.example.inventory.model.Product;
import com.example.inventory.model.Supplier;
import com.example.inventory.model.Supply;

public interface SupplyService {
    Supply recordSupply(Supplier supplier, Product product, int quantity, double cost);
}

