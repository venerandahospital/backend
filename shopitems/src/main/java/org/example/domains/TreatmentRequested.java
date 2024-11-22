package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "TreatmentRequested")
public class TreatmentRequested extends PanacheEntity {

    // Reference to the associated visit
    @ManyToOne
    @JoinColumn(nullable = false)
    public PatientVisit visit;

    // Quantity of lab tests requested
    @Column(nullable = false)
    public int quantity;

    // Unit price of the lab test
    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    // Total amount for the requested lab tests (unitPrice * quantity)
    @Column(nullable = false)
    public BigDecimal totalAmount;

    // Reference to the specific lab test being requested
    @ManyToOne
    @JoinColumn(nullable = false)
    public Item item;

}
