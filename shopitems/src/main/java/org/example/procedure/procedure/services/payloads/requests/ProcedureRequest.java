package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ProcedureRequest {

    @Schema(example = "malaria test")
    public String procedureType;

    @Schema(example = "mrdt")
    public String procedureName;

    @Schema(example = "labtest")
    public String category;

    @Schema(example = "check for pass cells")
    public String description;

    @Schema(example = "2000.0")
    public BigDecimal unitCostPrice;

    @Schema(example = "5000.0")
    public BigDecimal unitSellingPrice;

}
