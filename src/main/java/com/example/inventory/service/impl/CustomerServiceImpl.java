package com.example.inventory.service.impl;

import com.example.inventory.enums.UserRole;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.model.Account;
import com.example.inventory.model.Customer;
import com.example.inventory.repository.CustomerRepository;
import com.example.inventory.service.AccountService; // Use Interface, not Impl
import com.example.inventory.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {
    
    private final CustomerRepository customerRepository;
    private final AccountService accountService; // Changed to Interface

    public CustomerServiceImpl(CustomerRepository customerRepository, AccountService accountService) {
        this.customerRepository = customerRepository;
        this.accountService = accountService;
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
        // Look up the customer through their linked account's email
        return customerRepository.findByAccount_Email(email);
    }

    @Override
    @Transactional
    public Customer registerCustomer(String email, String password, String name, String phone) {
        Account newAccount = accountService.createAccount(email, password, UserRole.CUSTOMER);

        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setAccount(newAccount);

        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customerDetails) {
        return customerRepository.findById(id).map(existingCustomer -> {
            // Only update Profile data here
            if (customerDetails.getName() != null && !customerDetails.getName().trim().isEmpty()) {
                existingCustomer.setName(customerDetails.getName());
            }
            
            if (customerDetails.getPhone() != null && !customerDetails.getPhone().trim().isEmpty()) {
                existingCustomer.setPhone(customerDetails.getPhone());
            }
            
            if (customerDetails.getAddress() != null && !customerDetails.getAddress().trim().isEmpty()) {
                existingCustomer.setAddress(customerDetails.getAddress());
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