package com.example.inventory.controller;

import com.example.inventory.dto.request.AddNewProductRequestDTO;
import com.example.inventory.dto.request.SupplierRequestDTO;
import com.example.inventory.dto.response.SupplierSalesResponseDTO;
import com.example.inventory.dto.response.SupplierResponseDTO;
import com.example.inventory.dto.response.SupplyResponseDTO;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.mapper.SupplierMapper;
import com.example.inventory.mapper.SupplyMapper;
import com.example.inventory.model.Product;
import com.example.inventory.model.Supplier;
import com.example.inventory.model.Supply;
import com.example.inventory.service.SupplierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final SupplierMapper supplierMapper;
    private final SupplyMapper supplyMapper;

    public SupplierController(SupplierService supplierService, 
                              SupplierMapper supplierMapper,
                              SupplyMapper supplyMapper) {
        this.supplierService = supplierService;
        this.supplierMapper = supplierMapper;
        this.supplyMapper = supplyMapper;
    }

    // GET: /api/suppliers
    @GetMapping
    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        List<SupplierResponseDTO> suppliers = supplierService.getAllSuppliers()
                .stream()
                .map(supplierMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(suppliers);
    }

    // GET: /api/suppliers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        return ResponseEntity.ok(supplierMapper.toResponseDTO(supplier));
    }

    // GET: /api/suppliers/email/{email}
    @GetMapping("/email/{email}")
    public ResponseEntity<SupplierResponseDTO> getSupplierByEmail(@PathVariable String email) {
        Supplier supplier = supplierService.getSupplierByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with email: " + email));

        return ResponseEntity.ok(supplierMapper.toResponseDTO(supplier));
    }

    // POST: /api/suppliers
    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(@RequestBody SupplierRequestDTO requestDTO) {
        Supplier supplier = supplierMapper.toEntity(requestDTO);
        Supplier createdSupplier = supplierService.createSupplier(supplier);

        return ResponseEntity.status(HttpStatus.CREATED).body(supplierMapper.toResponseDTO(createdSupplier));
    }

    // PUT: /api/suppliers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(@PathVariable Long id, @RequestBody SupplierRequestDTO requestDTO) {
        Supplier supplierDetails = supplierMapper.toEntity(requestDTO);
        Supplier updatedSupplier = supplierService.updateSupplier(id, supplierDetails);

        return ResponseEntity.ok(supplierMapper.toResponseDTO(updatedSupplier));
    }

    // DELETE: /api/suppliers/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }


    // POST: /api/suppliers/{id}/products
    @PostMapping("/{id}/products")
    public ResponseEntity<SupplyResponseDTO> addNewProduct(
            @PathVariable Long id, 
            @RequestBody AddNewProductRequestDTO requestDTO) {
        
        // 1. Fetch the existing supplier
        Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        // 2. Map the DTO to a raw Product entity
        Product newProduct = new Product();
        newProduct.setTitle(requestDTO.getTitle());
        newProduct.setSize(requestDTO.getSize());
        newProduct.setDescription(requestDTO.getDescription());
        newProduct.setPrice(requestDTO.getPrice());

        // 3. Delegate to the service
        Supply supply = supplierService.addNewProduct(
                supplier, 
                newProduct, 
                requestDTO.getInitialQuantity(), 
                requestDTO.getCost()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(supplyMapper.toResponseDTO(supply));
    }

    @GetMapping("/{id}/supplies")
    public ResponseEntity<List<SupplyResponseDTO>> getSupplierProducts(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        List<SupplyResponseDTO> supplies = supplierService.getSuppliesBySupplier(supplier)
                .stream()
                .map(supplyMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(supplies);
    }

    @GetMapping("/{id}/sales")
    public ResponseEntity<List<SupplierSalesResponseDTO>> getSupplierSales(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        return ResponseEntity.ok(supplierService.getSalesBySupplier(supplier));
    }

    @PutMapping("/{id}/products/{productId}")
    public ResponseEntity<SupplyResponseDTO> updateSuppliedProduct(
            @PathVariable Long id,
            @PathVariable Long productId,
            @RequestBody AddNewProductRequestDTO requestDTO) {
        Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        Product productDetails = new Product();
        productDetails.setTitle(requestDTO.getTitle());
        productDetails.setSize(requestDTO.getSize());
        productDetails.setDescription(requestDTO.getDescription());
        productDetails.setPrice(requestDTO.getPrice());

        Supply supply = supplierService.updateSuppliedProduct(supplier, productId, productDetails, requestDTO.getCost());
        return ResponseEntity.ok(supplyMapper.toResponseDTO(supply));
    }

    @DeleteMapping("/{id}/products/{productId}")
    public ResponseEntity<Void> deleteSuppliedProduct(@PathVariable Long id, @PathVariable Long productId) {
        Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplierService.deleteSuppliedProduct(supplier, productId);
        return ResponseEntity.noContent().build();
    }
}
