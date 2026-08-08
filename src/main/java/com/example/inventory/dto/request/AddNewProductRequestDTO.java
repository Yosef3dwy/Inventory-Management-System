package com.example.inventory.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddNewProductRequestDTO {
    // Product Entity Details
    private String title;
    private int size;
    private String description;
    private double price;
    
    // Supply & Inventory Details
    private int initialQuantity;
    private double cost;
}