package org.example.procedure.procedure.services.payloads.responses;

import org.example.procedure.procedure.domains.Procedure;

import java.math.BigDecimal;

public class ProcedureDTO {
    public Long id;
    public String procedureName;
    public String procedureType;
    public String category;
    public String description;
    public BigDecimal unitCostPrice;
    public BigDecimal unitSellingPrice;
    //public List<ItemDTO> itemsUsed;

    public ProcedureDTO(Procedure labTest){
        this.id = labTest.id;
        this.procedureType = labTest.procedureType;
        this.procedureName = labTest.procedureName;
        this.category = labTest.category;
        this.description = labTest.description;
        this.unitCostPrice = labTest.unitCostPrice;
        this.unitSellingPrice = labTest.unitSellingPrice;
        // Mapping lists with proper null check and stream processing
       /*this.itemsUsed = labTest.getItemsUsed() != null ?
                labTest.getItemsUsed().stream()
                        .map(ItemDTO::new)
                        .collect(Collectors.toList()) : null;*/

    }

}
