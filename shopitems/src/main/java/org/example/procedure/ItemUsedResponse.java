package org.example.procedure;

import org.example.item.domain.Item;

import java.util.List;

public class ItemUsedResponse {
    public List<Item> items;
    public int totalQuantity;

    public ItemUsedResponse(List<Item> items, int totalQuantity) {
        this.items = items;
        this.totalQuantity = totalQuantity;
    }
}
