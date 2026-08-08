package com.example.inventory.service;

import com.example.inventory.model.Product;
import com.example.inventory.model.Supplier;
import com.example.inventory.model.Supply;

import java.util.List;
import java.util.Optional;

public interface SupplierService {

    // ==========================================
    // Supplier Entity Management (Account CRUD)
    // ==========================================
    List<Supplier> getAllSuppliers();
    
    Optional<Supplier> getSupplierById(Long id);
    
    Optional<Supplier> getSupplierByEmail(String email);
    
    Supplier createSupplier(Supplier supplier);
    
    Supplier updateSupplier(Long id, Supplier supplierDetails);
    
    void deleteSupplier(Long id);

    // ==========================================
    // Supply Catalog Management
    // ==========================================
    
    // Adds a brand-new product to the catalog, links it, and distributes stock
    Supply addNewProduct(Supplier supplier, Product newProduct, int initialQuantity, double cost);
}