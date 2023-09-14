package org.example.services;

import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.ShopItem;
import org.example.domains.repositories.ShopItemRepository;
import org.example.services.payloads.ShopItemRequest;
import org.example.services.payloads.ShopItemUpdateRequest;
import org.example.services.payloads.FullShopItemResponse;
import org.example.services.payloads.ShopItemParametersRequest;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.*;

@ApplicationScoped
public class ShopItemService {

    @Inject
    ShopItemRepository shopItemRepository;

    @Inject
    MySQLPool client;

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


    public List<ShopItem> searchItems(String category, String title) {
        if (category != null && title != null) {
            return shopItemRepository.list("category = ?1 AND title = ?2", category, title);
        } else if (category != null) {
            return shopItemRepository.list("category = ?1", category);
        } else if (title != null) {
            return shopItemRepository.list("title = ?1", title);
        } else {
            // If both parameters are null or empty, return all items.
            return shopItemRepository.listAll();
        }
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




    public List<FullShopItemResponse> getShopItemsAdvancedFilter(ShopItemParametersRequest request) {
           StringJoiner whereClause = getStringJoiner(request);

            String sql = """
                                   
                    SELECT
                    id,
                    category,
                    number,
                    image,
                    title,
                    price,
                    description
                    FROM shopitem
                    %s
                    ORDER BY creationDate DESC;                             
                    """.formatted(whereClause);

            return client.query(sql)
                    .execute()
                    .onItem()
                    .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                    .onItem()
                    .transform(this::from)
                    .collect().asList().await()
                    .indefinitely();

        }

    private FullShopItemResponse from(Row row){

        FullShopItemResponse response = new FullShopItemResponse();
        response.id = row.getLong("id");
        response.description = row.getString("description");
        response.image = row.getString("image");
        response.number = row.getString("number");
        response.category = row.getString("category");
        response.title = row.getString("title");
        response.price = row.getBigDecimal("price");
        return response;
    }

    private FullShopItemResponse fullShopItemDTO(ShopItem shopItem){
        FullShopItemResponse response = new FullShopItemResponse();
        response.id = shopItem.id;
        response.number = shopItem.number;
        response.price = shopItem.price;
        response.description = shopItem.description;
        response.category = shopItem.category;
        response.title = shopItem.title;
        response.creationDate = shopItem.creationDate;
        response.image = shopItem.image;



        return response;
    }



        private StringJoiner getStringJoiner(ShopItemParametersRequest request) {
            AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

            Map<String, String> searchCriteria = new HashMap<>();
            searchCriteria.put("category", request.category);
            searchCriteria.put("title", request.title);

            StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

            searchCriteria.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    whereClause.add(key + " = '" + value + "'");
                    hasSearchCriteria.set(Boolean.TRUE);
                }
            });

            if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
                whereClause.add("1 = 1");
            }
            return whereClause;
        }
    }



