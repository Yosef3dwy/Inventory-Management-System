package com.example.inventory.mapper;

import com.example.inventory.dto.response.CartItemResponseDTO;
import com.example.inventory.dto.response.CartResponseDTO;
import com.example.inventory.model.Cart;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    private final CartItemMapper cartItemMapper;

    public CartMapper(CartItemMapper cartItemMapper) {
        this.cartItemMapper = cartItemMapper;
    }

    public CartResponseDTO toResponseDTO(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartResponseDTO dto = new CartResponseDTO();
        dto.setCartId(cart.getCartId());

        if (cart.getCustomer() != null) {
            dto.setCustomerId(cart.getCustomer().getCustomerId());
        }

        if (cart.getItems() != null) {
            List<CartItemResponseDTO> itemDTOs = cart.getItems().stream()
                    .map(cartItemMapper::toResponseDTO)
                    .collect(Collectors.toList());
            
            dto.setItems(itemDTOs);

            // Calculate the total price of the entire cart dynamically
            double total = itemDTOs.stream()
                    .mapToDouble(CartItemResponseDTO::getSubTotal)
                    .sum();
            dto.setCartTotal(total);
        }

        return dto;
    }
}