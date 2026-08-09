package com.example.inventory.controller;

import com.example.inventory.dto.request.CustomerRequestDTO;
import com.example.inventory.dto.response.CustomerResponseDTO;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.mapper.CustomerMapper;
import com.example.inventory.model.Customer;
import com.example.inventory.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    public CustomerController(CustomerService customerService, CustomerMapper customerMapper) {
        this.customerService = customerService;
        this.customerMapper = customerMapper;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        List<CustomerResponseDTO> customers = customerService.getAllCustomers()
                .stream()
                .map(customerMapper::toResponseDTO)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
                
        return ResponseEntity.ok(customerMapper.toResponseDTO(customer));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponseDTO> getCustomerByEmail(@PathVariable String email) {
        Customer customer = customerService.getCustomerByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
                
        return ResponseEntity.ok(customerMapper.toResponseDTO(customer));
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(@RequestBody CustomerRequestDTO requestDTO) {
        Customer createdCustomer = customerService.registerCustomer(
                requestDTO.getEmail(),
                requestDTO.getPassword(),
                requestDTO.getName(),
                requestDTO.getPhone()
        );
        
        if (requestDTO.getAddress() != null && !requestDTO.getAddress().trim().isEmpty()) {
            createdCustomer.setAddress(requestDTO.getAddress());
            customerService.updateCustomer(createdCustomer.getCustomerId(), createdCustomer);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(customerMapper.toResponseDTO(createdCustomer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(@PathVariable Long id, @RequestBody CustomerRequestDTO requestDTO) {
        Customer customerDetails = customerMapper.toEntity(requestDTO);
        Customer updatedCustomer = customerService.updateCustomer(id, customerDetails);
        
        return ResponseEntity.ok(customerMapper.toResponseDTO(updatedCustomer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}