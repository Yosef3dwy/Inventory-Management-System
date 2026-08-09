package com.example.inventory.mapper;

import com.example.inventory.dto.request.CustomerRequestDTO;
import com.example.inventory.dto.response.CustomerResponseDTO;
import com.example.inventory.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponseDTO toResponseDTO(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setCustomerId(customer.getCustomerId());
        dto.setName(customer.getName());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        
        if (customer.getAccount() != null) {
            dto.setEmail(customer.getAccount().getEmail());
        }
        
        return dto;
    }

    public Customer toEntity(CustomerRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        
        return customer;
    }
}