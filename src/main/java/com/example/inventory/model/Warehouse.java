package com.example.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long WarehouseId;

    private int TotalCapacity;

    private String Location;

    private int FreeSpace;

    @OneToMany(mappedBy = "warehouse")
    private List<Inventory> inventory;

}
