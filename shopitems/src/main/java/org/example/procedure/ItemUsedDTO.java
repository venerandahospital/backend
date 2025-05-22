package org.example.procedure;

import java.math.BigDecimal;

public class ItemUsedDTO {
    public Long id;                  // ID of the ItemUsed entity
    public BigDecimal quantityUsed; // Quantity used
    public Long procedureId;        // Reference to Procedure ID
    public Long itemId;             // Reference to Item ID
    public String itemName;
    public  String procedureName;

    // Constructor to map ItemUsed to ItemUsedDTO
    public ItemUsedDTO(ItemUsed itemUsed) {
        this.id = itemUsed.id;
        this.quantityUsed = itemUsed.quantityUsed;
        this.procedureId = itemUsed.procedureId;
        this.itemId = itemUsed.itemId;
        this.itemName = itemUsed.itemName;
        this.procedureName = itemUsed.procedureName;
    }
}

