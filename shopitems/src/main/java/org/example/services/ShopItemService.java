package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.domains.ShopItem;
import org.example.domains.repositories.ShopItemRepository;
import org.example.services.payloads.ShopItemRequest;

import java.util.List;

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
        shopItem.image = request.image;

        shopItemRepository.persist(shopItem);

        return shopItem;

    }

    public List<ShopItem> getAllShopItems() {
        return shopItemRepository.listAll();
    }

    public void deleteAllShopItems(){
        shopItemRepository.deleteAll();
    }
}
