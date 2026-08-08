package com.example.inventory.mapper;

import com.example.inventory.dto.response.OrderItemResponseDTO;
import com.example.inventory.model.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    public OrderItemResponseDTO toResponseDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setOrderItemId(orderItem.getOrderItemId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setSubTotal(orderItem.getQuantity() * orderItem.getUnitPrice());

        if (orderItem.getProduct() != null) {
            dto.setProductId(orderItem.getProduct().getProductId());
            dto.setProductTitle(orderItem.getProduct().getTitle());
        }

        return dto;
    }
}