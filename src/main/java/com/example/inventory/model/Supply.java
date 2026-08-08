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
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SupplyId;

    private double Cost;

    @ManyToOne
    @JoinColumn(name = "SupplierId")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "ProductId")
    private Product product;

}
