package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class ShopItem extends PanacheEntity {

    @Column(nullable = false)
    public String number;

    @Column(nullable = false)
    public String category;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String description;

    @Column(nullable = false)
    public BigDecimal price;

    @Column
    public String image;

    @Column
    public LocalDateTime creationDate;

}
