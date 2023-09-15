package org.example.services.payloads;

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
}
