package com.example.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponseDTO {
    private Long warehouseId;
    private int totalCapacity;
    private String location;
    private int freeSpace;
}