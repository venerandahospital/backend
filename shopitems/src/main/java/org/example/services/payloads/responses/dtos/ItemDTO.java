package org.example.services.payloads.responses.dtos;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.domains.Item;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ItemDTO {
    public Long id;
    public String number;
    public String category;
    public String title;
    public String description;
    public BigDecimal costPrice;
    public BigDecimal sellingPrice;
    public String unitOfMeasure;
    public BigDecimal stockAtHand;

    public ItemDTO(Item item) {
        this.id = item.id;
        this.stockAtHand = item.stockAtHand;
        this.number = item.number;
        this.category = item.category;
        this.title = item.title;
        this.description = item.description;
        this.costPrice = item.costPrice;
        this.sellingPrice = item.sellingPrice;
        this.unitOfMeasure = item.unitOfMeasure;
    }
}
