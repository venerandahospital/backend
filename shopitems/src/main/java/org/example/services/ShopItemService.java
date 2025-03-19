package org.example.services;

import com.itextpdf.io.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

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
import org.example.domains.Invoice;
import org.example.domains.Item;
import org.example.domains.Procedure;
import org.example.domains.Stock;
import org.example.domains.repositories.ItemRepository;
import org.example.services.payloads.requests.ShopItemParametersRequest;
import org.example.services.payloads.requests.ShopItemRequest;
import org.example.services.payloads.requests.ShopItemUpdateRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import org.example.services.payloads.responses.basicResponses.FullShopItemResponse;
import org.example.services.payloads.responses.dtos.ItemDTO;
import org.example.services.payloads.responses.dtos.ProcedureDTO;

@ApplicationScoped
public class ShopItemService {

    @Inject
    ItemRepository shopItemRepository;

    @Inject
    MySQLPool client;

    @Inject
    UserAuthService userAuthService;

    private static final String NOT_FOUND = "Not found!";

    public Item addShopItem(ShopItemRequest request) {
        Item shopItem = new Item();
        shopItem.title = request.title;
        shopItem.number = userAuthService.generateRandomPassword(5);
        shopItem.category = request.category;
        shopItem.description = request.description;
        shopItem.costPrice = BigDecimal.valueOf(0);
        shopItem.sellingPrice = BigDecimal.valueOf(0);
        shopItem.image = request.image;
        shopItem.stockAtHand = 0;
        shopItem.unitOfMeasure = request.unitOfMeasure;
        shopItem.reOrderLevel = request.reOrderLevel;

        shopItem.creationDate = LocalDate.now();
        //shopItem.expiryDate = request.expiryDate;

        shopItemRepository.persist(shopItem);

        return shopItem;

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
        shopItemRepository.persist(item);
    }

    public void updateItemStockAtHandAfterSelling(Integer quantity, Item item) {

        // Update the invoice fields

        item.stockAtHand = item.stockAtHand-quantity;

        // Persist the updated invoice
        shopItemRepository.persist(item);
    }


    public void updateItemStockAtHandAfterDeleting(Integer quantity, Item item) {

        // Update the invoice fields

        item.stockAtHand = item.stockAtHand + quantity;

        // Persist the updated invoice
        shopItemRepository.persist(item);
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
                    number,
                    image,
                    title,
                    costPrice,
                    sellingPrice,
                    creationDate,
                    unitOfMeasure,
                    description
                    FROM item
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
        response.costPrice = row.getBigDecimal("costPrice");
        response.sellingPrice = row.getBigDecimal("sellingPrice");
        response.unitOfMeasure = row.getString("unitOfMeasure");
        response.creationDate = row.getLocalDate("creationDate");

        return response;
    }

    private FullShopItemResponse fullShopItemDTO(Item Item){
        FullShopItemResponse response = new FullShopItemResponse();
        response.id = Item.id;
        response.number = Item.number;
        response.costPrice = Item.costPrice;
        response.sellingPrice = Item.sellingPrice;
        response.description = Item.description;
        response.category = Item.category;
        response.unitOfMeasure = Item.unitOfMeasure;
        response.title = Item.title;
        response.creationDate =Item.creationDate;
        response.image = Item.image;

        return response;
    }

    private StringJoiner getStringJoiner(ShopItemParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.category != null && !request.category.isEmpty()) {
            conditions.add("category = '" + request.category + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.title != null && !request.title.isEmpty()) {
            conditions.add("title = '" + request.title + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.datefrom != null && request.dateto != null) {
            conditions.add("creationDate BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
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
        return shopItemRepository.listAll();
    }

    @Transactional
    public List<Item> listLatestFirst() {
        return shopItemRepository.listAll(Sort.descending("creationDate"));
    }

    public Item getShopItemById(Long id){
        return shopItemRepository.findById(id);
    }

    public void deleteAllShopItems(){
        shopItemRepository.deleteAll();

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








    public void deleteShopItemById(Long id){
        Item shopItem = shopItemRepository.findById(id);
        shopItem.delete();
    }


    public List<Item> searchItems(String category, String title) {
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


    public Item updateShopItemById(Long id, ShopItemUpdateRequest request) {
        return shopItemRepository.findByIdOptional(id)
                .map(shopItem -> {

                    shopItem.title = request.title;
                    shopItem.costPrice = request.costPrice;
                    shopItem.sellingPrice = request.sellingPrice;
                    shopItem.description = request.description;
                    shopItem.category = request.category;
                    shopItem.image = request.image;
                    shopItem.reOrderLevel = request.reOrderLevel;

                    shopItemRepository.persist(shopItem);

                    return shopItem;
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



