package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.domains.ShopItem;
import org.example.domains.repositories.ShopItemRepository;
import org.example.services.payloads.ShopItemRequest;

@ApplicationScoped
public class ShopItemService {

    @Inject
    ShopItemRepository shopItemRepository;

    public ShopItem addShopItem(ShopItemRequest request) {
        ShopItem shopItem = new ShopItem();
        shopItem.title = request.title;
        shopItem.number = request.number;
        shopItem.category = request.category;
        shopItem.description = request.description;
        shopItem.price = request.price;

        shopItemRepository.persist(shopItem);

        return shopItem;

    }
}
