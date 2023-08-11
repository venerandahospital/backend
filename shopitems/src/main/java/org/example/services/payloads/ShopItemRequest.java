package org.example.services.payloads;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ShopItemRequest {

    @Schema(example = "DXF20089")
    public String number;

    @Schema(example = "men's clothing")
    public String category;

    @Schema(example = "Men Cotton Jacket")
    public String title;

    @Schema(example = "Great outerwear jackets for Spring Autumn Winter, suitable for many occasions, such as working, hiking, camping, mountain rock climbing,")
    public String description;

    @Schema(example = "200")
    public BigDecimal price;
}
