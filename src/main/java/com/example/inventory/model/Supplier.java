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
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SupplierId;
    
    private String Name;

    private String phone;

    @OneToMany(mappedBy = "supplier")
    private List<Supply> supplies;

    @OneToOne
    @JoinColumn(name = "AccountID")
    private Account account;
}
