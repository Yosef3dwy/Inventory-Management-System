package com.example.inventory.mapper;

import com.example.inventory.dto.response.SupplyResponseDTO;
import com.example.inventory.model.Supply;
import org.springframework.stereotype.Component;

@Component
public class SupplyMapper {

    public SupplyResponseDTO toResponseDTO(Supply supply) {
        if (supply == null) {
            return null;
        }

        SupplyResponseDTO dto = new SupplyResponseDTO();
        dto.setSupplyId(supply.getSupplyId());
        dto.setCost(supply.getCost());
        
        if (supply.getSupplier() != null) {
            dto.setSupplierId(supply.getSupplier().getSupplierId());
        }
        
        if (supply.getProduct() != null) {
            dto.setProductId(supply.getProduct().getProductId());
            dto.setProductTitle(supply.getProduct().getTitle());
        }

        return dto;
    }
}