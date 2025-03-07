package org.example.services;

import io.quarkus.panache.common.Sort;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.domains.Item;
import org.example.domains.Stock;
import org.example.domains.Store;
import org.example.domains.repositories.ItemRepository;
import org.example.domains.repositories.StockRepository;
import org.example.domains.repositories.StoreRepository;
import org.example.services.payloads.requests.StockTakeRequest;
import org.example.services.payloads.responses.dtos.StockDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@ApplicationScoped
public class StockService {

    @Inject
    StockRepository stockRepository;

    @Inject
    ItemRepository itemRepository;

    @Inject
    StoreRepository storeRepository;

    @Inject
    ShopItemService itemService;

    @Inject
    MySQLPool client;

    public static final String INVALID_REQUEST = "Invalid request data!";


    public StockDTO receiveStock(StockTakeRequest request) {

        Item item = itemRepository.findById(request.itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found for ID: " + request.itemId);
        }

        Store store = storeRepository.findById(request.storeId);
        if (store == null) {
            throw new IllegalArgumentException("store not found for ID: " + request.storeId);
        }

        // Calculate unit cost
        BigDecimal unitCost = request.totalCostPrice.divide(BigDecimal.valueOf(request.quantityReceived), RoundingMode.HALF_UP);

        Stock stock = new Stock();
        stock.item = item;
        stock.totalCostPrice = request.totalCostPrice;
        stock.quantityReceived = request.quantityReceived;
        stock.unitCostPrice = unitCost; // Assuming unitSellingPrice corresponds to unitCost
        stock.quantityAvailable = item.stockAtHand; // Assuming all received stock is available initially
        stock.newQuantity = request.quantityReceived + item.stockAtHand; // Assuming newQuantity equals the received quantity
        stock.expiryDate = request.expiryDate;
        stock.brand = request.brand;
        stock.store = store;
        stock.packaging = request.packaging;
        stock.unitSellingPrice = request.unitSellingPrice;
        stock.receiveDate = request.receiveDate; // Assuming stock is received on the current date
        // Persist the stock
        stockRepository.persist(stock);

        // Update the item's stock at hand
        itemService.updateItemStockAtHand(stock, item);

        // Return the StockDTO
        return new StockDTO(stock);
    }

    @Transactional
    public List<Stock> getAllStockReceives() {
        return stockRepository.listAll(Sort.descending("receiveDate"));
    }




}
