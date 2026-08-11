package com.example.inventory.controller;

import com.example.inventory.dto.request.CartItemRequestDTO;
import com.example.inventory.dto.response.CartResponseDTO;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.mapper.CartMapper;
import com.example.inventory.model.Cart;
import com.example.inventory.model.Customer;
import com.example.inventory.model.Product;
import com.example.inventory.service.CartService;
import com.example.inventory.service.CustomerService;
import com.example.inventory.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts/{customerId}")
public class CartController {

    private final CartService cartService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final CartMapper cartMapper;

    public CartController(CartService cartService, 
                          CustomerService customerService, 
                          ProductService productService, 
                          CartMapper cartMapper) {
        this.cartService = cartService;
        this.customerService = customerService;
        this.productService = productService;
        this.cartMapper = cartMapper;
    }



    // GET: /api/carts/{customerId}
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long customerId) {
            Customer customer = customerService.getCustomerById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
            
            Cart cart = cartService.getOrCreateCart(customer);
            return ResponseEntity.ok(cartMapper.toResponseDTO(cart));
        }
        
    // POST: /api/carts/{customerId}/add
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addItemToCart(
            @PathVariable Long customerId, 
            @RequestBody CartItemRequestDTO requestDTO) {
        
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Product product = productService.getProductById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + requestDTO.getProductId())); 

        cartService.addItem(customer, product, requestDTO.getQuantity());
        
        // Fetch the updated cart to return to the user
        Cart updatedCart = cartService.getOrCreateCart(customer);
        return ResponseEntity.ok(cartMapper.toResponseDTO(updatedCart));
    }

    // PUT: /api/carts/{customerId}/update
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @PutMapping("/update")
    public ResponseEntity<CartResponseDTO> updateItemQuantity(
            @PathVariable Long customerId, 
            @RequestBody CartItemRequestDTO requestDTO) {
        
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Product product = productService.getProductById(requestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + requestDTO.getProductId()));

        cartService.updateItemQuantity(customer, product, requestDTO.getQuantity());

        Cart updatedCart = cartService.getOrCreateCart(customer);
        return ResponseEntity.ok(cartMapper.toResponseDTO(updatedCart));
    }

    // DELETE: /api/carts/{customerId}/remove/{productId}
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponseDTO> removeItemFromCart(
            @PathVariable Long customerId, 
            @PathVariable Long productId) {
        
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        cartService.removeItem(customer, product);

        Cart updatedCart = cartService.getOrCreateCart(customer);
        return ResponseEntity.ok(cartMapper.toResponseDTO(updatedCart));
    }

    // DELETE: /api/carts/{customerId}/clear
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long customerId) {
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
                
        cartService.clearCart(customer);
        
        return ResponseEntity.noContent().build();
    }
}