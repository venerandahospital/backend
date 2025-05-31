package org.example.treatment.services.payloads.responses;

import org.example.treatment.domains.TreatmentRequested;

import java.math.BigDecimal;

public class TreatmentRequestedDTO {
    public Long id;
    public BigDecimal quantity;
    public BigDecimal unitSellingPrice;
    public BigDecimal totalAmount;
    public BigDecimal provisionalTotalAmount;
    public String itemName;
    public BigDecimal provisionalQuantity;


    public TreatmentRequestedDTO(TreatmentRequested treatmentRequested) {
        this.id = treatmentRequested.id;
        this.quantity = treatmentRequested.quantity;
        this.provisionalQuantity = treatmentRequested.provisionalQuantity;
        this.unitSellingPrice = treatmentRequested.unitSellingPrice;
        this.provisionalTotalAmount = treatmentRequested.provisionalTotalAmount;
        this.totalAmount = treatmentRequested.totalAmount;
        this.itemName = treatmentRequested.itemName;

    }
}
