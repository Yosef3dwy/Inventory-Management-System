package com.example.inventory.repository;

import com.example.inventory.model.Product;
import com.example.inventory.model.Supplier;
import com.example.inventory.model.Supply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
    // Which suppliers have supplied this product and when
    List<Supply> findByProduct(Product product);

    // A supplier's supply history
    List<Supply> findBySupplier(Supplier supplier);
}
