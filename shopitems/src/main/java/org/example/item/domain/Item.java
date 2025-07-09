package org.example.item.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Item extends PanacheEntity {

    @Column
    public String number;

    @Column
    public String category;

    @Column
    public String subCategory;

    @Column
    public String subSubSubCategory;

    @Column
    public String indication;

    @Column
    public String subSubCategory;

    @Column
    public String formulation; // e.g. "Tablet", "Syrup", "Injection"

    @Column
    public String activeIngredient;

    @Column
    public String strength;

    @Column
    public String manufacturer;

    @Column
    public String batchNumber;

    @Column(nullable = false)
    public String title;

    @Column
    public String genericName;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column
    public BigDecimal costPrice;

    @Column
    public BigDecimal sellingPrice;

    @Column
    public String image;

    @Column
    public BigDecimal stockAtHand;

    @Column
    public Integer reOrderLevel;

    @Column
    public Integer shelfNumber;

    @Column
    public String unitOfMeasure;

    @Column
    public String brand;

    @Column
    public String packaging;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;


}
