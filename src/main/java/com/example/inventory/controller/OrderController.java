package com.example.inventory.controller;

import com.example.inventory.dto.request.OrderStatusUpdateRequestDTO;
import com.example.inventory.dto.response.OrderResponseDTO;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.mapper.OrderMapper;
import com.example.inventory.model.Customer;
import com.example.inventory.model.Order;
import com.example.inventory.service.CustomerService;
import com.example.inventory.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, 
                           CustomerService customerService, 
                           OrderMapper orderMapper) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.orderMapper = orderMapper;
    }

    

    // POST: /api/orders/checkout/{customerId}
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @PostMapping("/checkout/{customerId}")
    public ResponseEntity<OrderResponseDTO> checkout(@PathVariable Long customerId) {
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        
        Order newOrder = orderService.checkout(customer);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponseDTO(newOrder));
    }

    // GET: /api/orders/customer/{customerId}
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrderHistory(Principal principal) {
        
        // 1. principal.getName() automatically extracts the email from the JWT token
        String userEmail = principal.getName();
        
        // 2. Fetch the customer using the secure email
        Customer customer = customerService.getCustomerByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        // 3. Get their specific history
        List<OrderResponseDTO> history = orderService.getOrderHistory(customer)
                .stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(history);
    }

    // GET: /api/orders/all
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrdersInSystem() {
        
        List<OrderResponseDTO> allOrders = orderService.getAllOrders()
                .stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(allOrders);
    }

    // PATCH: /api/orders/{orderId}/status
    // Want to KNOW how to handle this function either from the front end or the back end, I will put an API for it for now.
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER')")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId, 
            @RequestBody OrderStatusUpdateRequestDTO requestDTO) {
        
        Order updatedOrder = orderService.updateStatus(
                orderId, 
                requestDTO.getStatus(), 
                requestDTO.getDeliveredDate()
        );
        
        return ResponseEntity.ok(orderMapper.toResponseDTO(updatedOrder));
    }
}