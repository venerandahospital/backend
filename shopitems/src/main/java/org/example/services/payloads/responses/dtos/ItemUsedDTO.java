package org.example.services.payloads.responses.dtos;

import org.example.domains.ItemUsed;
import java.math.BigDecimal;

public class ItemUsedDTO {
    public Long id;         // ID of the ItemUsed entity
    public int quantity;    // Quantity used
    public Long labTestId;  // Reference to LabTest ID
    public Long itemId;     // Reference to Item ID
    public BigDecimal total; // Total amount

    // Constructor to map ItemUsed to ItemUsedDTO
    public ItemUsedDTO(ItemUsed itemUsed) {
        this.id = itemUsed.id;
        this.quantity = itemUsed.quantity;
        this.labTestId = itemUsed.labTest != null ? itemUsed.labTest.id : null;
        this.itemId = itemUsed.item != null ? itemUsed.item.id : null;
        this.total = itemUsed.total;
    }
}
