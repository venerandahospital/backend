package org.example.treatment.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.example.visit.domains.PatientVisit;

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
    public BigDecimal quantity;

    @Column(nullable = false)
    public BigDecimal provisionalQuantity;


    @Column
    public BigDecimal amountPerFrequencyValue;

    @Column
    public String amountPerFrequencyUnit;

    @Column
    public BigDecimal frequencyValue;

    @Column
    public String frequencyUnit;

    @Column
    public BigDecimal durationValue;

    @Column
    public String durationUnit;

    @Column
    public BigDecimal lastUnitValue;

    @Column
    public BigDecimal totalUnits;


    @Column
    public String instructions;

    @Column
    public String route;

    // Unit price of the lab test
    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    // Total amount for the requested lab tests (unitPrice * quantity)
    @Column(nullable = false)
    public BigDecimal totalAmount;

    @Column
    public Integer shelfNumber;

    @Column(nullable = false)
    public BigDecimal provisionalTotalAmount;

    // Reference to the specific lab test being requested
    @Column(nullable = false)
    public String itemName;

    @Column
    public BigDecimal lastStockAtHand;

    // Reference to the specific lab test being requested
    @Column
    public BigDecimal unitBuy;

    @Column
    public BigDecimal availableQuantity;

    @Column
    public BigDecimal lastUpDateQuantity;

    // Reference to the specific lab test being requested
    @Column(nullable = false)
    public Long itemId;

    @Column
    public String status;



}
