package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "cart")
public class ShoppingCart extends PanacheEntity {

    public int quantity;

    @ManyToOne
    public User user;

    @ManyToOne
    public Item shopItem;
}
