package com.example.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplyResponseDTO {
    private Long supplyId;
    private Long supplierId;
    private Long productId;
    private String productTitle;
    private double cost;
}