package com.example.inventory.controller;

import com.example.inventory.dto.request.StockRequestDTO;
import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.mapper.WarehouseMapper;
import com.example.inventory.model.Product;
import com.example.inventory.model.Warehouse;
import com.example.inventory.repository.WarehouseRepository;
import com.example.inventory.service.InventoryService;
import com.example.inventory.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    public InventoryController(InventoryService inventoryService, 
                               ProductService productService,
                               WarehouseRepository warehouseRepository,
                               WarehouseMapper warehouseMapper) {
        this.inventoryService = inventoryService;
        this.productService = productService;
        this.warehouseRepository = warehouseRepository;
        this.warehouseMapper = warehouseMapper;
    }


    // POST: /api/inventory/reserve
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER')")
    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveStock(@RequestBody StockRequestDTO requestDTO) {
        Product product = productService.getProductById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + requestDTO.getProductId()));
        
        inventoryService.reserveStock(product, requestDTO.getQuantity());
        return ResponseEntity.ok().build();
    }

    // POST: /api/inventory/restock
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER')")
    @PostMapping("/restock")
    public ResponseEntity<Void> restock(@RequestBody StockRequestDTO requestDTO) {
        Product product = productService.getProductById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + requestDTO.getProductId()));

        inventoryService.restock(product, requestDTO.getQuantity());
        return ResponseEntity.ok().build();
    }

    // GET: /api/inventory/warehouses
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> getAllWarehouses() {
        List<WarehouseResponseDTO> warehouses = inventoryService.getAllWarehouses()
                .stream()
                .map(warehouseMapper::toResponseDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(warehouses);
    }

    // POST: /api/inventory/warehouses
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponseDTO> addWarehouse(@RequestBody WarehouseRequestDTO requestDTO) {
        Warehouse warehouse = warehouseMapper.toEntity(requestDTO);
        inventoryService.addWarehouse(warehouse);
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseMapper.toResponseDTO(warehouse));
    }

    // POST: /api/inventory/warehouses/{id}/clear
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/warehouses/{id}/clear")
    public ResponseEntity<Void> clearWarehouse(@PathVariable Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        inventoryService.clearWarehouse(warehouse);
        return ResponseEntity.ok().build();
    }

    // DELETE: /api/inventory/warehouses/{id}
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/warehouses/{id}")
    public ResponseEntity<Void> removeWarehouse(@PathVariable Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        inventoryService.removeWarehouse(warehouse);
        return ResponseEntity.noContent().build();
    }
}