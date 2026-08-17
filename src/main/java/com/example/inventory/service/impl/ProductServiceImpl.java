package com.example.inventory.service.impl;

import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.InvalidInputException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Inventory;
import com.example.inventory.model.Product;
import com.example.inventory.model.Warehouse;
import com.example.inventory.repository.CartItemRepository;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.OrderItemRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.SupplyRepository;
import com.example.inventory.repository.WarehouseRepository;
import com.example.inventory.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final SupplyRepository supplyRepository;
    private final WarehouseRepository warehouseRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CartItemRepository cartItemRepository,
                              InventoryRepository inventoryRepository,
                              OrderItemRepository orderItemRepository,
                              SupplyRepository supplyRepository,
                              WarehouseRepository warehouseRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.supplyRepository = supplyRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product createProduct(Product product) {
        if (product.getSize() <= 0) {
            throw new InvalidInputException("Product size must be greater than zero to be stored in a warehouse.");
        }
        
        if (productRepository.findByTitle(product.getTitle()).isPresent()) {
            throw new DuplicateResourceException("A product with the title '" + product.getTitle() + "' already exists in the catalog.");
        }

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product productDetails) {
        if (productDetails.getSize() <= 0) {
            throw new InvalidInputException("Product size must be greater than zero.");
        }

        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setTitle(productDetails.getTitle());
            existingProduct.setSize(productDetails.getSize()); // Int value for warehouse distribution
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setPrice(productDetails.getPrice());
            
            return productRepository.save(existingProduct);
        }).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (!orderItemRepository.findByProduct(product).isEmpty()) {
            throw new InvalidInputException("Cannot delete product because it exists in order history.");
        }

        cartItemRepository.deleteByProduct(product);
        for (Inventory inventory : inventoryRepository.findByProduct(product)) {
            Warehouse warehouse = inventory.getWarehouse();
            warehouse.setFreeSpace(warehouse.getFreeSpace() + (inventory.getQuantity() * product.getSize()));
            warehouseRepository.save(warehouse);
        }
        inventoryRepository.deleteByProduct(product);
        supplyRepository.deleteByProduct(product);
        productRepository.delete(product);
    }
}
