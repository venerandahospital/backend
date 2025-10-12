package org.example.item.services;

import com.itextpdf.io.IOException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.auth.services.UserAuthService;
import org.example.configuration.handler.ResponseMessage;
import org.example.item.domain.Item;
import org.example.item.domain.repositories.ItemRepository;
import org.example.item.services.payloads.requests.ShopItemParametersRequest;
import org.example.item.services.payloads.requests.ShopItemRequest;
import org.example.item.services.payloads.requests.ShopItemUpdateRequest;
import org.example.item.services.payloads.responses.FullShopItemResponse;
import org.example.item.services.payloads.responses.ItemDTO;
import org.example.item.services.payloads.responses.ItemQuantityDto;
import org.example.stock.domains.Stock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShopItemService {

    @Inject
    ItemRepository itemRepository;

    @Inject
    MySQLPool client;

    @Inject
    UserAuthService userAuthService;

    private static final String NOT_FOUND = "Not found!";

    @Transactional
    public Item addShopItem(ShopItemRequest request) {
        Item shopItem = new Item();
        shopItem.title = request.title;
        shopItem.number = userAuthService.generateRandomPassword(5);
        shopItem.category = request.category;
        shopItem.shelfNumber = request.shelfNumber;
        shopItem.subCategory = request.subCategory;
        shopItem.description = request.description;
        shopItem.costPrice = request.costPrice != null ? request.costPrice : BigDecimal.valueOf(0);
        shopItem.sellingPrice = request.sellingPrice != null ? request.sellingPrice : BigDecimal.valueOf(0);
        shopItem.stockAtHand = request.stockAtHand != null ? request.stockAtHand : BigDecimal.valueOf(0);

        shopItem.image = request.image;

        shopItem.lastUnitOfMeasure = request.lastUnitOfMeasure;
        shopItem.lastUnitValue = request.lastUnitValue;



        shopItem.unitOfMeasure = request.unitOfMeasure;
        shopItem.reOrderLevel = request.reOrderLevel;

        shopItem.creationDate = LocalDate.now();
        //shopItem.expiryDate = request.expiryDate;

        itemRepository.persist(shopItem);

        return shopItem;

    }

    @Transactional
    public List<Item> getAllItemsWithStockAtHandBelowReOrderLevels() {
        return itemRepository.find("stockAtHand <= reOrderLevel", Sort.descending("id"))
                .list();
    }



    @Transactional
    public List<Item> addShopItems(List<ShopItemRequest> requests) {
        List<Item> createdItems = new ArrayList<>();

        for (ShopItemRequest request : requests) {
            Item shopItem = new Item();
            shopItem.title = request.title;
            shopItem.shelfNumber = request.shelfNumber;
            shopItem.number = userAuthService.generateRandomPassword(5);
            shopItem.category = request.category;
            shopItem.subCategory = request.subCategory;
            shopItem.lastUnitOfMeasure = request.lastUnitOfMeasure;
            shopItem.lastUnitValue = request.lastUnitValue;
            shopItem.description = request.description;
            shopItem.costPrice = request.costPrice != null ? request.costPrice : BigDecimal.valueOf(0);
            shopItem.sellingPrice = request.sellingPrice != null ? request.sellingPrice : BigDecimal.valueOf(0);
            shopItem.stockAtHand = request.stockAtHand != null ? request.stockAtHand : BigDecimal.valueOf(0);

            shopItem.image = request.image;
            shopItem.unitOfMeasure = request.unitOfMeasure;
            shopItem.reOrderLevel = request.reOrderLevel;
            shopItem.creationDate = LocalDate.now();

            itemRepository.persist(shopItem);
            createdItems.add(shopItem);
        }

        return createdItems;
    }



    public void updateItemStockAtHand(Stock stock, Item item) {

        // Update the invoice fields
        item.stockAtHand = stock.newQuantity ;
        item.expiryDate = stock.expiryDate;
        item.costPrice = stock.unitCostPrice;


        item.sellingPrice = stock.unitSellingPrice;
        item.brand = stock.brand;
        item.packaging = stock.packaging;

        // Persist the updated invoice
        itemRepository.persist(item);
    }

    public void updateItemStockAtHandAfterSelling(BigDecimal quantity, Item item) {
        // Update the stock at hand
        item.stockAtHand = item.stockAtHand.subtract(quantity);

        // Persist the updated item
        itemRepository.persist(item);
    }

    public void updateItemStockAtHandBeforeUpdating(BigDecimal quantity, Item item) {
        // Update the stock at hand
        item.stockAtHand = item.stockAtHand.add(quantity);

        // Persist the updated item
        itemRepository.persist(item);
    }




    public Response updateItemStockAtHandAfterService(List<ItemQuantityDto> itemsUsed) {
        for (ItemQuantityDto dto : itemsUsed) {
            Item item = itemRepository.find("title = ?1", dto.itemName).firstResult();

            if (item != null) {
                // Subtract the quantity from stockAtHand
                item.stockAtHand = item.stockAtHand.subtract(BigDecimal.valueOf(dto.quantity));
                itemRepository.persist(item);
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Item not found: " + dto.itemName, null))
                        .build();
            }
        }

        return Response.ok(new ResponseMessage("Items used updated successfully")).build();
    }




    public void updateItemStockAtHandAfterDeleting(BigDecimal quantity, Item item) {
        // Update the stock at hand
        item.stockAtHand = item.stockAtHand.add(quantity);

        // Persist the updated item
        itemRepository.persist(item);
    }



    @Transactional
    public Response generateAndReturnPdf(ShopItemParametersRequest request) {
        //List<ShopItem> shopItems = shopItemRepository.listAll(Sort.ascending("category", "title"));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);

            Document document = new Document(pdfDocument);

            Table table = new Table(6);
            table.setWidthPercent(100);

            Cell[] headerCells = {
                    createCell("Number"),
                    createCell("Category"),
                    createCell("Title"),
                    createCell("Description"),
                    createCell("CostPrice"),
                    createCell("Creation Date")

            };

            for (Cell cell : headerCells) {
                cell.setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }

            for (FullShopItemResponse Item : getShopItemsAdvancedFilter(request)) {
                table.addCell(createCell(Item.number));
                table.addCell(createCell(Item.category));
                table.addCell(createCell(Item.title));
                table.addCell(createCell(Item.description));
                table.addCell(createCell("$" + Item.costPrice.toString()));
                table.addCell(createCell(Item.creationDate.toString()));
            }

            document.add(table);

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=shop_items.pdf")
                    .type("application/pdf")
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Cell createCell(String content) {
        return new Cell().add(content);
    }


    public List<FullShopItemResponse> getShopItemsAdvancedFilter(ShopItemParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
        SELECT
            id,
            category,
            subCategory,
            number,
            image,
            title,
            costPrice,
            sellingPrice,
            creationDate,
            unitOfMeasure,
            description
        FROM vena.Item
        %s
        ORDER BY creationDate DESC;
        """.formatted(whereClause);

        return client.query(sql)
                .execute()
                .onItem()
                .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem()
                .transform(this::from)
                .collect().asList()
                .await()
                .indefinitely();
    }


    private FullShopItemResponse from(Row row){

        FullShopItemResponse response = new FullShopItemResponse();
        response.id = row.getLong("id");
        response.description = row.getString("description");
        response.image = row.getString("image");
        response.number = row.getString("number");
        response.category = row.getString("category");
        response.subCategory = row.getString("subCategory");
        response.title = row.getString("title");
        response.costPrice = row.getBigDecimal("costPrice");
        response.sellingPrice = row.getBigDecimal("sellingPrice");
        response.unitOfMeasure = row.getString("unitOfMeasure");
        response.creationDate = row.getLocalDate("creationDate");

        return response;
    }

    private StringJoiner getStringJoiner(ShopItemParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.category != null && !request.category.isEmpty()) {
            conditions.add("category = '" + request.category + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.subCategory != null && !request.subCategory.isEmpty()) {
            conditions.add("subCategory = '" + request.subCategory + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.title != null && !request.title.isEmpty()) {
            conditions.add("title = '" + request.title + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.datefrom != null && request.dateto != null) {
            conditions.add("expiryDate BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }



    public List<Item> getAllShopItems() {
        return itemRepository.listAll();
    }

    @Transactional
    public List<ItemDTO> listLatestFirst() {
        return itemRepository
                .listAll(Sort.descending("id"))
                .stream()
                .map(ItemDTO::new) // or use a custom mapper: item -> new ItemDTO(item)
                .collect(Collectors.toList());
    }


    public Item getShopItemById(Long id){
        return itemRepository.findById(id);
    }

    public void deleteAllShopItems(){
        itemRepository.deleteAll();

    }

    public List<ItemDTO> getDrugItems(){
        List<Item> items = Item.find(
                "category = ?1 ORDER BY id DESC",
                "drug"
        ).list();

        // Convert ProcedureRequested entities to ProcedureDTO
        return items.stream()
                .map(ItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response deleteShopItemById(Long id){
        Item item = itemRepository.findById(id);
        if (item == null) {
            //return Response.status(Response.Status.NOT_FOUND).build();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("item not found", null))
                    .build();
        }
        itemRepository.delete(item);
        return Response.ok(new ResponseMessage("Item Deleted successfully")).build();
    }


    public List<Item> searchItems(String category, String title) {
        if (category != null && title != null) {
            return itemRepository.list("category = ?1 AND title = ?2", category, title);
        } else if (category != null) {
            return itemRepository.list("category = ?1", category);
        } else if (title != null) {
            return itemRepository.list("title = ?1", title);
        } else {
            // If both parameters are null or empty, return all items.
            return itemRepository.listAll();
        }
    }


    public ItemDTO updateShopItemById(Long id, ShopItemUpdateRequest request) {
        return itemRepository.findByIdOptional(id)
                .map(shopItem -> {

                    shopItem.title = request.title;
                    shopItem.description = request.description;
                    shopItem.shelfNumber = request.shelfNumber;
                    shopItem.category = request.category;
                    shopItem.subCategory = request.subCategory;
                    shopItem.lastUnitOfMeasure = request.lastUnitOfMeasure;
                    shopItem.lastUnitValue = request.lastUnitValue;
                    shopItem.image = request.image;
                    shopItem.reOrderLevel = request.reOrderLevel;
                    shopItem.unitOfMeasure = request.unitOfMeasure;
                    shopItem.sellingPrice = request.sellingPrice != null ? request.sellingPrice : BigDecimal.valueOf(0);
                    shopItem.stockAtHand = request.stockAtHand != null ? request.stockAtHand : BigDecimal.valueOf(0);

                    itemRepository.persist(shopItem);

                    return new ItemDTO(shopItem);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }


        private StringJoiner getStringJoinerSimplified(ShopItemParametersRequest request) {
            AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

            Map<String, String> searchCriteria = new HashMap<>();
            searchCriteria.put("category", request.category);
            searchCriteria.put("title", request.title);
            searchCriteria.put("datefrom", String.valueOf(request.datefrom));
            searchCriteria.put("dateto", String.valueOf(request.dateto));


            StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

            searchCriteria.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    if ("datefrom".equals(key) || "dateto".equals(key)) {
                        whereClause.add("creationDate BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
                    } else {
                        whereClause.add(key + " = '" + value + "'");
                    }
                    hasSearchCriteria.set(Boolean.TRUE);
                }
            });
            if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
                whereClause.add("1 = 1");
            }
            return whereClause;
        }
    }



