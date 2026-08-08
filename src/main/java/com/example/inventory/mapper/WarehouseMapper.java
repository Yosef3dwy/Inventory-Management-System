package com.example.inventory.mapper;

import com.example.inventory.dto.request.WarehouseRequestDTO;
import com.example.inventory.dto.response.WarehouseResponseDTO;
import com.example.inventory.model.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {

    public Warehouse toEntity(WarehouseRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setTotalCapacity(dto.getTotalCapacity());
        warehouse.setLocation(dto.getLocation());
        
        return warehouse;
    }

    public WarehouseResponseDTO toResponseDTO(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }

        WarehouseResponseDTO dto = new WarehouseResponseDTO();
        dto.setWarehouseId(warehouse.getWarehouseId());
        dto.setTotalCapacity(warehouse.getTotalCapacity());
        dto.setLocation(warehouse.getLocation());
        dto.setFreeSpace(warehouse.getFreeSpace());
        
        return dto;
    }
}