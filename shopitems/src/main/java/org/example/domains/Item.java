package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Item extends PanacheEntity {

    @Column(nullable = false)
    public String number;

    @Column(nullable = false)
    public String category;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String description;

    @Column(nullable = false)
    public BigDecimal costPrice;

    @Column(nullable = false)
    public BigDecimal sellingPrice;

    @Column
    public String image;

    @Column
    public String unitOfMeasure;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

}
