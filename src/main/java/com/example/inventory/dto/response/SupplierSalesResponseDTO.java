package com.example.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplierSalesResponseDTO {
    private Long productId;
    private String productTitle;
    private int quantitySold;
    private double revenue;
}
