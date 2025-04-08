package org.example.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ProcedureRequestedRequest {

    @Schema(example = "1")
    public int quantity;

    @Schema(example = "1")
    public Long procedureId;

    @Schema(example = "Low HB")
    public String report;

    @Schema(example = "Dr.Judith")
    public String orderedBy;

    @Schema(example = "collin muwanguzi")
    public String doneBy;

    @Schema(example = "10000")
    public BigDecimal unitSellingPrice;










}
