package com.example.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long InventoryId;

    private int Quantity;

    @ManyToOne
    @JoinColumn(name = "WarehouseId")
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "ProductId")
    private Product product;

}
