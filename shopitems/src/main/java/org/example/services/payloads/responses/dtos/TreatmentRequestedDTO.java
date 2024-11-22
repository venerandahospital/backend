package org.example.services.payloads.responses.dtos;

import org.example.domains.TreatmentRequested;
import java.math.BigDecimal;

public class TreatmentRequestedDTO {
    public Long id;
    public int quantity;
    public BigDecimal unitSellingPrice;
    public BigDecimal totalAmount;
    public ItemDTO item;


    public TreatmentRequestedDTO(TreatmentRequested treatmentRequested) {
        this.id = treatmentRequested.id;
        this.quantity = treatmentRequested.quantity;
        this.unitSellingPrice = treatmentRequested.unitSellingPrice;
        this.totalAmount = treatmentRequested.totalAmount;
        this.item = new ItemDTO(treatmentRequested.item);

    }
}
