package org.example.services.payloads.responses.dtos;

import org.example.domains.ProcedureRequested;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ProcedureRequestedDTO {
    public Long id;
    public Integer quantity;
    public BigDecimal unitSellingPrice;
    public BigDecimal totalAmount;
    public String report;
    public String orderedBy;
    public String doneBy;
    public Long visitId;
    public LocalDate dateOfProcedure;
    public LocalTime timeOfProcedure;
    public String procedureRequestedType;
    public String category;

    // Constructor to map LabTestsRequested to LabTestsRequestedDTO
    public ProcedureRequestedDTO(ProcedureRequested procedureRequested) {
        this.dateOfProcedure = procedureRequested.dateOfProcedure;
        this.timeOfProcedure = procedureRequested.timeOfProcedure;

        this.id = procedureRequested.id;
        this.quantity = procedureRequested.quantity;
        this.unitSellingPrice = procedureRequested.unitSellingPrice;
        this.totalAmount = procedureRequested.totalAmount;
        this.orderedBy = procedureRequested.orderedBy;
        this.report = procedureRequested.report;
        this.doneBy = procedureRequested.doneBy;
        this.procedureRequestedType = procedureRequested.procedureRequestedType;
        this.category = procedureRequested.category;

        // Properly reference the instance variable for visit
        this.visitId = procedureRequested.visit != null ? procedureRequested.visit.id : null;

        // Map the LabTest entity to LabTestDTO
    }
}
