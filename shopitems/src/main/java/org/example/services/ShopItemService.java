package org.example.services;

import com.itextpdf.io.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Text;

import com.itextpdf.layout.property.TextAlignment;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.auth.services.UserAuthService;
import org.example.domains.ShopItem;
import org.example.domains.repositories.ShopItemRepository;
import org.example.services.payloads.FullShopItemResponse;
import org.example.services.payloads.ShopItemParametersRequest;
import org.example.services.payloads.ShopItemRequest;
import org.example.services.payloads.ShopItemUpdateRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;

import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import io.quarkus.panache.common.Sort;
import java.math.BigDecimal;
import java.util.List;

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
                    createCell("Price"),
                    createCell("Creation Date")

            };

            for (Cell cell : headerCells) {
                cell.setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }

            for (FullShopItemResponse shopItem : getShopItemsAdvancedFilter(request)) {
                table.addCell(createCell(shopItem.number));
                table.addCell(createCell(shopItem.category));
                table.addCell(createCell(shopItem.title));
                table.addCell(createCell(shopItem.description));
                table.addCell(createCell("$" + shopItem.price.toString()));
                table.addCell(createCell(shopItem.creationDate.toString()));
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


    ///// now generating excell sheet ////////////////////////////

    /*public Response generateExcelFile(List<ShopItem> shopItems) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Shop Items");

                // Create headers
                Row headerRow = (Row) sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Number");
                headerRow.createCell(1).setCellValue("Category");
                headerRow.createCell(2).setCellValue("Title");
                headerRow.createCell(3).setCellValue("Description");
                headerRow.createCell(4).setCellValue("Price");
                headerRow.createCell(5).setCellValue("Image");
                headerRow.createCell(6).setCellValue("Creation Date");

                // Populate data
                int rowNum = 1;
                for (ShopItem item : shopItems) {
                    Row row = (Row) sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(item.number);
                    row.createCell(1).setCellValue(item.category);
                    row.createCell(2).setCellValue(item.title);
                    row.createCell(3).setCellValue(item.description);
                    row.createCell(4).setCellValue(item.price.doubleValue());
                    row.createCell(5).setCellValue(item.image);
                    row.createCell(6).setCellValue(item.creationDate.toString());
                }

                // Create a temporary file
                File tempFile = File.createTempFile("shop_items", ".xlsx");

                // Save the Excel file to the temporary file
                try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    workbook.write(outputStream);
                }

                // Create a response with the temporary file as an entity
                Response.ResponseBuilder response = Response.ok(tempFile);
                response.header("Content-Disposition", "attachment; filename=shop_items.xlsx");
                response.header("Content-Length", String.valueOf(tempFile.length()));
                response.type(MediaType.APPLICATION_OCTET_STREAM);

                // Delete the temporary file after it's downloaded
                tempFile.deleteOnExit();

                return response.build();
            } catch (IOException | java.io.IOException e) {
                // Handle exceptions
                e.printStackTrace();
                return Response.serverError().entity("Error generating Excel file").build();
            }
        }*/


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



