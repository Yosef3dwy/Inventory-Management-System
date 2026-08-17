package com.example.inventory.controller;

import com.example.inventory.dto.request.OrderStatusUpdateRequestDTO;
import com.example.inventory.dto.response.OrderResponseDTO;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.mapper.OrderMapper;
import com.example.inventory.model.Customer;
import com.example.inventory.model.Order;
import com.example.inventory.security.AuthUser;
import com.example.inventory.service.CustomerService;
import com.example.inventory.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders()
                .stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orders);
    }

    // POST: /api/orders/checkout/{customerId}
    @PostMapping("/checkout/{customerId}")
    public ResponseEntity<OrderResponseDTO> checkout(@PathVariable Long customerId) {
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        
        Order newOrder = orderService.checkout(customer);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponseDTO(newOrder));
    }

    // GET: /api/orders/customer/{customerId}
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrderHistory(@PathVariable Long customerId) {
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        
        List<OrderResponseDTO> history = orderService.getOrderHistory(customer)
                .stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(history);
    }

    // PATCH: /api/orders/{orderId}/status
    // Want to how to handle this function either from the front end or the back end, I will put an API for it for now.
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

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId, jakarta.servlet.http.HttpServletRequest request) {
        AuthUser authUser = (AuthUser) request.getAttribute("authUser");
        Customer customer = null;

        if (authUser != null && authUser.role() == com.example.inventory.enums.UserRole.CUSTOMER) {
            customer = customerService.getCustomerById(authUser.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + authUser.userId()));
        }

        Order cancelledOrder = orderService.cancelOrder(orderId, customer);
        return ResponseEntity.ok(orderMapper.toResponseDTO(cancelledOrder));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
