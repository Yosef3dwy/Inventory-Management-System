package com.example.inventory.controller;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.mapper.ProductMapper;
import com.example.inventory.model.Product;
import com.example.inventory.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    // GET: /api/products
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts()
                .stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(products);
    }

    // GET: /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
                
        return ResponseEntity.ok(productMapper.toResponseDTO(product));
    }

    // POST: /api/products (Admin level creation without linking the product to a  supplier)
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO requestDTO) {
        Product product = productMapper.toEntity(requestDTO);
        Product createdProduct = productService.createProduct(product);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponseDTO(createdProduct));
    }

    // PUT: /api/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDTO requestDTO) {
        Product productDetails = productMapper.toEntity(requestDTO);
        Product updatedProduct = productService.updateProduct(id, productDetails);
        
        return ResponseEntity.ok(productMapper.toResponseDTO(updatedProduct));
    }
}