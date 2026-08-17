package com.example.inventory.service.impl;

import com.example.inventory.dto.response.SupplierSalesResponseDTO;
import com.example.inventory.exception.CapacityExceededException;
import com.example.inventory.exception.InvalidInputException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.OrderItem;
import com.example.inventory.model.Product;
import com.example.inventory.model.Supplier;
import com.example.inventory.model.Supply;
import com.example.inventory.repository.OrderItemRepository;
import com.example.inventory.repository.SupplierRepository;
import com.example.inventory.repository.SupplyRepository;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.ProductService;
import com.example.inventory.service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplyRepository supplyRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

    public SupplierServiceImpl(SupplierRepository supplierRepository,
                               SupplyRepository supplyRepository,
                               OrderItemRepository orderItemRepository,
                               ProductService productService, 
                               InventoryService inventoryService) {
        this.supplierRepository = supplierRepository;
        this.supplyRepository = supplyRepository;
        this.orderItemRepository = orderItemRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
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
        return supplierRepository.findByEmail(email);
    }

    @Override
    public Supplier createSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier updateSupplier(Long id, Supplier supplierDetails) {
        return supplierRepository.findById(id).map(existingSupplier -> {
            if (supplierDetails.getName() != null && !supplierDetails.getName().trim().isEmpty()) {
                existingSupplier.setName(supplierDetails.getName());
            }
            
            if (supplierDetails.getEmail() != null && !supplierDetails.getEmail().trim().isEmpty()) {
                existingSupplier.setEmail(supplierDetails.getEmail());
            }
            
            if (supplierDetails.getPhone() != null && !supplierDetails.getPhone().trim().isEmpty()) {
                existingSupplier.setPhone(supplierDetails.getPhone());
            }
            
            if (supplierDetails.getPassword() != null && !supplierDetails.getPassword().trim().isEmpty()) {
                existingSupplier.setPassword(supplierDetails.getPassword());
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

    @Override
    @Transactional(readOnly = true)
    public List<Supply> getSuppliesBySupplier(Supplier supplier) {
        if (supplier == null) throw new InvalidInputException("Supplier cannot be null");
        return supplyRepository.findBySupplier(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierSalesResponseDTO> getSalesBySupplier(Supplier supplier) {
        List<Product> suppliedProducts = getSuppliesBySupplier(supplier).stream()
                .map(Supply::getProduct)
                .toList();

        if (suppliedProducts.isEmpty()) {
            return List.of();
        }

        return orderItemRepository.findByProductIn(suppliedProducts).stream()
                .collect(Collectors.groupingBy(OrderItem::getProduct))
                .entrySet()
                .stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    int quantitySold = entry.getValue().stream().mapToInt(OrderItem::getQuantity).sum();
                    double revenue = entry.getValue().stream()
                            .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                            .sum();
                    return new SupplierSalesResponseDTO(product.getProductId(), product.getTitle(), quantitySold, revenue);
                })
                .toList();
    }

    @Override
    public Supply updateSuppliedProduct(Supplier supplier, Long productId, Product productDetails, double cost) {
        if (supplier == null) throw new InvalidInputException("Supplier cannot be null");
        if (productId == null) throw new InvalidInputException("Product ID cannot be null");
        if (cost < 0) throw new InvalidInputException("Cost cannot be negative");

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Supply supply = supplyRepository.findBySupplierAndProduct(supplier, product)
                .orElseThrow(() -> new InvalidInputException("This product does not belong to the supplier."));

        productService.updateProduct(productId, productDetails);
        supply.setCost(cost);
        return supplyRepository.save(supply);
    }

    @Override
    public void deleteSuppliedProduct(Supplier supplier, Long productId) {
        if (supplier == null) throw new InvalidInputException("Supplier cannot be null");
        if (productId == null) throw new InvalidInputException("Product ID cannot be null");

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        supplyRepository.findBySupplierAndProduct(supplier, product)
                .orElseThrow(() -> new InvalidInputException("This product does not belong to the supplier."));

        productService.deleteProduct(productId);
    }

}
