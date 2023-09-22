package org.example.services;

import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;


import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.example.auth.services.UserAuthService;
import org.example.domains.ShopItem;
import org.example.domains.repositories.ShopItemRepository;
import org.example.services.payloads.FullShopItemResponse;
import org.example.services.payloads.ShopItemParametersRequest;
import org.example.services.payloads.ShopItemRequest;
import org.example.services.payloads.ShopItemUpdateRequest;


import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;













@ApplicationScoped
public class ShopItemService {

    @Inject
    ShopItemRepository shopItemRepository;

    @Inject
    MySQLPool client;

    @Inject
    UserAuthService userAuthService;

    private static final String NOT_FOUND = "Not found!";

    public ShopItem addShopItem(ShopItemRequest request) {
        ShopItem shopItem = new ShopItem();
        shopItem.title = request.title;
        shopItem.number = userAuthService.generateRandomPassword(5);
        shopItem.category = request.category;
        shopItem.description = request.description;
        shopItem.price = request.price;
        shopItem.image = request.image;

        shopItem.creationDate = LocalDate.now();

        shopItemRepository.persist(shopItem);
        return shopItem;

    }








   /* public Response generatePdf() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();
            document.add(new Paragraph("Hello, PDF from Quarkus!"));

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return Response.ok(pdfBytes)
                    .header("Content-Disposition", "attachment; filename=quarkus_pdf.pdf")
                    .build();
        } catch (DocumentException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @Transactional
    public Response generateAndReturnPdf() {
        List<ShopItem> shopItems = shopItemRepository.listAll(Sort.ascending("category", "title"));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Add content to the PDF
            for (ShopItem shopItem : shopItems) {
                Paragraph paragraph = new Paragraph();
                paragraph.add("Number: " + shopItem.number);
                paragraph.add("\n");
                paragraph.add("Category: " + shopItem.category);
                paragraph.add("\n");
                paragraph.add("Title: " + shopItem.title);
                paragraph.add("\n");
                paragraph.add("Description: " + shopItem.description);
                paragraph.add("\n");
                paragraph.add("Price: $" + shopItem.price.toString());
                paragraph.add("\n");
                paragraph.add("\n"); // Add space between items
                document.add(paragraph);
            }

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=shop_items.pdf")
                    .type("application/pdf")
                    .build();
        } catch (DocumentException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }*/




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
                    creationDate,
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
        response.creationDate = row.getLocalDate("creationDate");

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



