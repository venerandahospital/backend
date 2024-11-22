package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class ItemUsed extends PanacheEntity {

    public int quantity;

    @ManyToOne
    public Procedure labTest;

    @ManyToOne
    public Item item;

    @Column
    public BigDecimal total;




}
