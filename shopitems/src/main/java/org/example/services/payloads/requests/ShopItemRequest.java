package org.example.services.payloads.requests;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ShopItemRequest {

    @Schema(example = "Electronics")
    public String category;

    @Schema(example = "Men Cotton Jacket")
    public String title;

    @Schema(example = "This is the best and cheapest ladies hand bag ont he market ang that arches with most dresse")
    public String description;

    @Schema(example = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2Fcomputer.jpg?alt=media&token=54728992-5370-4be3-91d2-05e54bac6042")
    public String image;

    @Schema(example = "Tab")
    public String unitOfMeasure;

    @Schema(example = "50")
    public Integer reOrderLevel;






}
