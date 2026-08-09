package com.example.inventory.service;

import java.util.List;
import java.util.Optional;

import com.example.inventory.model.Customer;

public interface CustomerService {

    public List<Customer> getAllCustomers();

    public Optional<Customer> getCustomerById(Long id);

    public Optional<Customer> getCustomerByEmail(String email);

    Customer registerCustomer(String email, String password, String name, String phone);

    public Customer updateCustomer(Long id, Customer customerDetails);

    public void deleteCustomer(Long id);

}
