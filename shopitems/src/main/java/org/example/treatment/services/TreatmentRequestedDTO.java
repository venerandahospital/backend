package org.example.treatment.services;

import org.example.treatment.domains.TreatmentRequested;

import java.math.BigDecimal;

public class TreatmentRequestedDTO {
    public Long id;
    public BigDecimal quantity;
    public BigDecimal unitSellingPrice;
    public BigDecimal totalAmount;
    public String itemName;


    public TreatmentRequestedDTO(TreatmentRequested treatmentRequested) {
        this.id = treatmentRequested.id;
        this.quantity = treatmentRequested.quantity;
        this.unitSellingPrice = treatmentRequested.unitSellingPrice;
        this.totalAmount = treatmentRequested.totalAmount;
        this.itemName = treatmentRequested.itemName;

    }
}
