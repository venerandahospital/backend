package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Stock extends PanacheEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    public Item item;

    @ManyToOne
    @JoinColumn(name = "store_id")
    public Store store;

    @Column(nullable = false)
    public Integer quantityReceived;

    @Column(nullable = false)
    public BigDecimal unitCostPrice;

    @Column(nullable = false)
    public BigDecimal totalCostPrice;

    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    @Column(nullable = false)
    public Integer quantityAvailable;

    @Column(nullable = false)
    public Integer newQuantity;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate receiveDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate expiryDate;

    @Column
    public String brand;

    @Column
    public String packaging;
}
