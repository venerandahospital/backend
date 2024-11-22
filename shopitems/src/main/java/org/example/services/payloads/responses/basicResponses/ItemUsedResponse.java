package org.example.services.payloads.responses.basicResponses;

import org.example.domains.Item;

import java.util.List;

public class ItemUsedResponse {
    public List<Item> items;
    public int totalQuantity;

    public ItemUsedResponse(List<Item> items, int totalQuantity) {
        this.items = items;
        this.totalQuantity = totalQuantity;
    }
}
