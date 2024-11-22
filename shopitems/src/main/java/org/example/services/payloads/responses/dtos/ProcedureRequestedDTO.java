package org.example.services.payloads.responses.dtos;

import org.example.domains.ProcedureRequested;
import java.math.BigDecimal;

public class ProcedureRequestedDTO {
    public Long id;
    public Integer quantity;
    public BigDecimal unitSellingPrice;
    public BigDecimal totalAmount;
    public String Report;
    public String orderedBy;
    public String doneBy;
    public Long visitId;
    public ProcedureDTO procedure;

    // Constructor to map LabTestsRequested to LabTestsRequestedDTO
    public ProcedureRequestedDTO(ProcedureRequested procedureRequested) {
        this.id = procedureRequested.id;
        this.quantity = procedureRequested.quantity;
        this.unitSellingPrice = procedureRequested.unitSellingPrice;
        this.totalAmount = procedureRequested.totalAmount;
        this.orderedBy = procedureRequested.orderedBy;
        this.Report = procedureRequested.Report;
        this.doneBy = procedureRequested.doneBy;

        // Properly reference the instance variable for visit
        this.visitId = procedureRequested.visit != null ? procedureRequested.visit.id : null;

        // Map the LabTest entity to LabTestDTO
        this.procedure = procedureRequested.procedure != null ? new ProcedureDTO(procedureRequested.procedure) : null;
    }
}
