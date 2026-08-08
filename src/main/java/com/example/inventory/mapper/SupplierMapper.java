package com.example.inventory.mapper;

import com.example.inventory.dto.request.SupplierRequestDTO;
import com.example.inventory.dto.response.SupplierResponseDTO;
import com.example.inventory.model.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierResponseDTO toResponseDTO(Supplier supplier) {
        if (supplier == null) {
            return null;
        }

        SupplierResponseDTO dto = new SupplierResponseDTO();
        dto.setSupplierId(supplier.getSupplierId());
        dto.setName(supplier.getName());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        
        return dto;
    }

    public Supplier toEntity(SupplierRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setEmail(dto.getEmail());
        supplier.setPassword(dto.getPassword());
        supplier.setPhone(dto.getPhone());
        
        return supplier;
    }
}