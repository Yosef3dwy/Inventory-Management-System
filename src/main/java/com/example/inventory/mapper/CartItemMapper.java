package com.example.inventory.mapper;

import com.example.inventory.dto.response.CartItemResponseDTO;
import com.example.inventory.model.CartItem;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {

    public CartItemResponseDTO toResponseDTO(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.setCartItemId(cartItem.getCartItemId());
        dto.setQuantity(cartItem.getQuantity());

        if (cartItem.getProduct() != null) {
            dto.setProductId(cartItem.getProduct().getProductId());
            dto.setProductTitle(cartItem.getProduct().getTitle());
            dto.setUnitPrice(cartItem.getProduct().getPrice());
            dto.setSubTotal(cartItem.getQuantity() * cartItem.getProduct().getPrice());
        }

        return dto;
    }
}