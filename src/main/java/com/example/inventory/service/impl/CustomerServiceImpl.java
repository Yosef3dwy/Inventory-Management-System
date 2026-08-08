package com.example.inventory.service.impl;

import com.example.inventory.exception.DuplicateResourceException;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Customer;
import com.example.inventory.repository.CustomerRepository;
import com.example.inventory.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {
    
    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public Customer createCustomer(Customer customer) {
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new DuplicateResourceException("A customer with this email address already exists.");
        }

        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customerDetails) {
        return customerRepository.findById(id).map(existingCustomer -> {
            if (customerDetails.getName() != null && !customerDetails.getName().trim().isEmpty()) {
                existingCustomer.setName(customerDetails.getName());
            }
            
            if (customerDetails.getEmail() != null && !customerDetails.getEmail().trim().isEmpty()) {
                existingCustomer.setEmail(customerDetails.getEmail());
            }
            
            if (customerDetails.getPhone() != null && !customerDetails.getPhone().trim().isEmpty()) {
                existingCustomer.setPhone(customerDetails.getPhone());
            }
            
            if (customerDetails.getAddress() != null && !customerDetails.getAddress().trim().isEmpty()) {
                existingCustomer.setAddress(customerDetails.getAddress());
            }
            
            if (customerDetails.getPassword() != null && !customerDetails.getPassword().trim().isEmpty()) {
                existingCustomer.setPassword(customerDetails.getPassword());
            }
            
            return customerRepository.save(existingCustomer);
        }).orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }
}