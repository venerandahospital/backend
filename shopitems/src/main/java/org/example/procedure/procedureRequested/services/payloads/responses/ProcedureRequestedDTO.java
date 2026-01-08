package org.example.procedure.procedureRequested.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ProcedureRequestedDTO {
    public Long id;
    public Long visitId;
    public String procedureRequestedType;
    public String procedureRequestedName;
    public String category;
    public int quantity;
    public BigDecimal unitSellingPrice;
    public BigDecimal totalAmount;
    public String report;
    public String status;
    public String bgColor;
    public String patientName;
    public String orderedBy;
    public String doneBy;
    public String exam;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate dateOfProcedure;
    
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;
    
    public LocalTime timeOfProcedure;
    public Long procedureId;

    public ProcedureRequestedDTO(ProcedureRequested procedureRequested) {
        if (procedureRequested != null) {
            this.id = procedureRequested.id;
            this.visitId = procedureRequested.visit != null ? procedureRequested.visit.id : null;
            this.procedureRequestedType = procedureRequested.procedureRequestedType;
            this.procedureRequestedName = procedureRequested.procedureRequestedName;
            this.category = procedureRequested.category;
            this.quantity = procedureRequested.quantity;
            this.unitSellingPrice = procedureRequested.unitSellingPrice;
            this.totalAmount = procedureRequested.totalAmount;
            this.report = procedureRequested.report;
            this.status = procedureRequested.status;
            this.bgColor = procedureRequested.bgColor;
            this.patientName = procedureRequested.patientName;
            this.orderedBy = procedureRequested.orderedBy;
            this.doneBy = procedureRequested.doneBy;
            this.exam = procedureRequested.exam;
            this.dateOfProcedure = procedureRequested.dateOfProcedure;
            this.updateDate = procedureRequested.updateDate;
            this.timeOfProcedure = procedureRequested.timeOfProcedure;
            this.procedureId = procedureRequested.procedureId;
        }
    }
}




