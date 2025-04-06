package org.example.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ProcedureRequest {

    @Schema(example = "LabTest")
    public String procedureType;

    @Schema(example = "LabTest")
    public String category;

    @Schema(example = "check for pass cells")
    public String description;

    @Schema(example = "2000.0")
    public BigDecimal unitCostPrice;

    @Schema(example = "5000.0")
    public BigDecimal unitSellingPrice;

}
