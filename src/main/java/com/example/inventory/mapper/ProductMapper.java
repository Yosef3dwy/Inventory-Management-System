package com.example.inventory.mapper;

import com.example.inventory.dto.request.ProductRequestDTO;
import com.example.inventory.dto.response.ProductResponseDTO;
import com.example.inventory.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(product.getProductId());
        dto.setTitle(product.getTitle());
        dto.setSize(product.getSize());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        
        return dto;
    }

    public Product toEntity(ProductRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setSize(dto.getSize());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        
        return product;
    }
}