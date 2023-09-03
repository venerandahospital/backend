package org.example.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.ShopItem;
import org.example.domains.repositories.ShopItemRepository;
import org.example.services.payloads.ShopItemRequest;
import org.example.services.payloads.ShopItemUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.listAll;

@ApplicationScoped
public class ShopItemService {

    @Inject
    ShopItemRepository shopItemRepository;

    private static final String NOT_FOUND = "Not found!";

    public ShopItem addShopItem(ShopItemRequest request) {
        ShopItem shopItem = new ShopItem();
        shopItem.title = request.title;
        shopItem.number = request.number;
        shopItem.category = request.category;
        shopItem.description = request.description;
        shopItem.price = request.price;
        shopItem.image = request.image;
        shopItem.creationDate = LocalDateTime.now();

        shopItemRepository.persist(shopItem);
        return shopItem;

    }

    public List<ShopItem> getAllShopItems() {
        return shopItemRepository.listAll();
    }

    @Transactional
    public List<ShopItem> listLatestFirst() {
        return shopItemRepository.listAll(Sort.descending("creationDate"));
    }

    public ShopItem getShopItemById(Long id){
        return shopItemRepository.findById(id);
    }

    public void deleteAllShopItems(){
        shopItemRepository.deleteAll();

    }
    public void deleteShopItemById(Long id){
        ShopItem shopItem = shopItemRepository.findById(id);
        shopItem.delete();
    }

    public ShopItem updateShopItemById(Long id, ShopItemUpdateRequest request) {
        return shopItemRepository.findByIdOptional(id)
                .map(shopItem -> {
                    shopItem.number = request.number;
                    shopItem.title = request.title;
                    shopItem.price = request.price;
                    shopItem.description = request.description;
                    shopItem.category = request.category;
                    shopItem.image = request.image;

                    shopItemRepository.persist(shopItem);

                    return shopItem;
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }


}
