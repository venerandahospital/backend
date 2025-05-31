package org.example.store.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Store extends PanacheEntity {

    @Column
    public String name;

    @Column
    public String location;

    @Column
    public String description;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

}


