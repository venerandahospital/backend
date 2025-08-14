package org.example.procedure.procedureRequested.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ProcedureRequested")
public class ProcedureRequested extends PanacheEntity {

    // Reference to the associated visit
    @ManyToOne
    @JoinColumn(nullable = false)
    public PatientVisit visit;

    @Column(nullable = false)
    public String procedureRequestedType;

    @Column(nullable = false)
    public String category;

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
    public String report;

    @Column
    public String status;

    @Column
    public String patientName;

    // Name or ID of the person who ordered the lab test
    @Column
    public String orderedBy;

    // Name or ID of the person who performed or is responsible for the lab test
    @Column
    public String doneBy;

    @Column
    public String exam;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfProcedure;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;

    @Column
    public LocalTime timeOfProcedure;

    @Column
    public Long procedureId;

}

