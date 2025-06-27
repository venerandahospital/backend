package org.example.item.services.payloads.responses;

import org.example.item.domain.Item;

import java.math.BigDecimal;

public class ItemDTO {
    public Long id;
    public String number;
    public String category;
    public String subCategory;
    public String title;
    public Integer shelfNumber;
    public String description;
    public BigDecimal costPrice;
    public BigDecimal sellingPrice;
    public String unitOfMeasure;
    public BigDecimal stockAtHand;

    public ItemDTO(Item item) {
        this.id = item.id;
        this.shelfNumber = item.shelfNumber;
        this.stockAtHand = item.stockAtHand;
        this.number = item.number;
        this.category = item.category;
        this.subCategory = item.subCategory;
        this.title = item.title;
        this.description = item.description;
        this.costPrice = item.costPrice;
        this.sellingPrice = item.sellingPrice;
        this.unitOfMeasure = item.unitOfMeasure;
    }
}
