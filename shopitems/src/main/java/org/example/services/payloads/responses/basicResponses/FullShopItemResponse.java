package org.example.services.payloads.responses.basicResponses;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FullShopItemResponse {

    public Long id;

    public String category;

    public String title;

    public String description;

    public LocalDate creationDate;

    public String number;

    public String image;

    public BigDecimal price;

    public BigDecimal costPrice;

    public BigDecimal sellingPrice;
}
