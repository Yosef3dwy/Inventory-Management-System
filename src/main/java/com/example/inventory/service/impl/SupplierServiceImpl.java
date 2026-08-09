package com.example.inventory.service.impl;

import com.example.inventory.enums.UserRole;
import com.example.inventory.exception.CapacityExceededException;
import com.example.inventory.exception.InvalidInputException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Account;
import com.example.inventory.model.Product;
import com.example.inventory.model.Supplier;
import com.example.inventory.model.Supply;
import com.example.inventory.repository.SupplierRepository;
import com.example.inventory.repository.SupplyRepository;
import com.example.inventory.service.AccountService;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.ProductService;
import com.example.inventory.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplyRepository supplyRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final AccountService accountService; // Inject AccountService

    public SupplierServiceImpl(SupplierRepository supplierRepository,
                               SupplyRepository supplyRepository, 
                               ProductService productService, 
                               InventoryService inventoryService,
                               AccountService accountService) {
        this.supplierRepository = supplierRepository;
        this.supplyRepository = supplyRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.accountService = accountService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Supplier> getSupplierByEmail(String email) {
        // Look up the supplier through their linked account's email
        return supplierRepository.findByAccount_Email(email);
    }

    @Override
    @Transactional
    public Supplier registerSupplier(String email, String password, String name, String phone) {
        // 1. Create the secure Account first
        Account newAccount = accountService.createAccount(email, password, UserRole.SUPPLIER);

        // 2. Create the Supplier Profile and link it to the Account
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setPhone(phone);
        supplier.setAccount(newAccount);

        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        return supplierRepository.findById(id).map(existingSupplier -> {
            if (supplierDetails.getName() != null && !supplierDetails.getName().trim().isEmpty()) {
                existingSupplier.setName(supplierDetails.getName());
            }
            
            if (supplierDetails.getPhone() != null && !supplierDetails.getPhone().trim().isEmpty()) {
                existingSupplier.setPhone(supplierDetails.getPhone());
            }
            
            return supplierRepository.save(existingSupplier);
        }).orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    @Override
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }

    @Override
    public Supply addNewProduct(Supplier supplier, Product newProduct, int initialQuantity, double cost) {
        if (supplier == null) throw new InvalidInputException("Supplier cannot be null");
        if (newProduct == null) throw new InvalidInputException("Product cannot be null");
        if (initialQuantity < 0) throw new InvalidInputException("Initial quantity cannot be negative");
        if (cost < 0) throw new InvalidInputException("Cost cannot be negative");

        Product savedProduct = productService.createProduct(newProduct);

        Supply supply = new Supply();
        supply.setSupplier(supplier);
        supply.setProduct(savedProduct);
        supply.setCost(cost);

        Supply savedSupply = supplyRepository.save(supply);

        if (initialQuantity > 0) {
            int leftover = inventoryService.distributeToWarehouses(savedProduct, initialQuantity);
            
            if (leftover > 0) {
                throw new CapacityExceededException(
                        "Failed to add new product. Not enough warehouse space to store " + leftover + " items."
                );
            }
        }

        return savedSupply;
    }
}