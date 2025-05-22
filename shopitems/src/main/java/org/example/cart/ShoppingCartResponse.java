package org.example.cart;

import org.example.item.domain.Item;

import java.util.List;

public class ShoppingCartResponse {

    public List<Item> items;
    public int totalQuantity;

    public ShoppingCartResponse(List<Item> items, int totalQuantity) {
        this.items = items;
        this.totalQuantity = totalQuantity;
    }

}


