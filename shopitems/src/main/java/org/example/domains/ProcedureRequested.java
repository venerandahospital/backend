package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ProcedureRequested")
public class ProcedureRequested extends PanacheEntity {

    // Reference to the associated visit
    @ManyToOne
    @JoinColumn(nullable = false)
    public PatientVisit visit;

    // Reference to the specific lab test being requested
    @ManyToOne
    @JoinColumn(nullable = false)
    public Procedure procedure;

    // Quantity of lab tests requested
    @Column(nullable = false)
    public int quantity;

    // Unit price of the lab test
    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    // Total amount for the requested lab tests (unitPrice * quantity)
    @Column(nullable = false)
    public BigDecimal totalAmount;

    @Column
    public String Report;

    // Name or ID of the person who ordered the lab test
    @Column
    public String orderedBy;

    // Name or ID of the person who performed or is responsible for the lab test
    @Column
    public String doneBy;
}

