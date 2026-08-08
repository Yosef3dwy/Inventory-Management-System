package com.example.inventory.mapper;

import com.example.inventory.dto.response.OrderItemResponseDTO;
import com.example.inventory.dto.response.OrderResponseDTO;
import com.example.inventory.model.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;

    public OrderMapper(OrderItemMapper orderItemMapper) {
        this.orderItemMapper = orderItemMapper;
    }

    public OrderResponseDTO toResponseDTO(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setDeliveredDate(order.getDeliveredDate());

        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getCustomerId());
        }

        if (order.getItems() != null) {
            List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                    .map(orderItemMapper::toResponseDTO)
                    .collect(Collectors.toList());

            dto.setItems(itemDTOs);

            // Calculate the total cost of the order dynamically
            double total = itemDTOs.stream()
                    .mapToDouble(OrderItemResponseDTO::getSubTotal)
                    .sum();
            dto.setOrderTotal(total);
        }

        return dto;
    }
}