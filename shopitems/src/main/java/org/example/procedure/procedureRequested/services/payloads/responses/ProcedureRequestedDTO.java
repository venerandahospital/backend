package org.example.procedure.procedureRequested.services.payloads.responses;

import org.example.procedure.procedureRequested.domains.ProcedureRequested;

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
    public String exam;
    public String patientName;
    public String status;
    public LocalDate dateOfProcedure;
    public LocalTime timeOfProcedure;
    public String procedureRequestedType;
    public String procedureRequestedName;
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
        this.exam = procedureRequested.exam;
        this.status = procedureRequested.status;
        this.patientName = procedureRequested.visit.patient.patientFirstName+" "+procedureRequested.visit.patient.patientSecondName;

        this.procedureRequestedType = procedureRequested.procedureRequestedType;
        this.procedureRequestedName = procedureRequested.procedureRequestedName;
        this.category = procedureRequested.category;

        // Properly reference the instance variable for visit
        this.visitId = procedureRequested.visit.id;

        // Map the LabTest entity to LabTestDTO
    }
}
