package com.example.inventory.service;

import com.example.inventory.model.Product;
import com.example.inventory.model.Supplier;
import com.example.inventory.model.Supply;

import java.util.List;
import java.util.Optional;

public interface SupplierService {

    List<Supplier> getAllSuppliers();
    
    Optional<Supplier> getSupplierById(Long id);
    
    Optional<Supplier> getSupplierByEmail(String email);

    List<Supply> getProductsBySupplier(Supplier supplier);
    
    Supplier registerSupplier(String email, String password, String name, String phone);
    
    Supplier updateSupplier(Long id, Supplier supplierDetails);
    
    void deleteSupplier(Long id);

    Supply addNewProduct(Supplier supplier, Product newProduct, int initialQuantity, double cost);
}