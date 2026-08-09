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
        dto.setPhone(supplier.getPhone());
        
        if (supplier.getAccount() != null) {
            dto.setEmail(supplier.getAccount().getEmail());
        }
        
        return dto;
    }

    public Supplier toEntity(SupplierRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setPhone(dto.getPhone());
        
        return supplier;
    }
}