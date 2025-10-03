package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

public class ProcedureCategoryRequest {

    @Schema(example = "labTest")
    public String procedureCategory;

    @Schema(example = "for laTest")
    public String categoryDescription;


}
