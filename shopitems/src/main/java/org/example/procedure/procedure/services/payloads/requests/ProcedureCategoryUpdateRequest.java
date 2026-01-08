package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ProcedureCategoryUpdateRequest {
    @Schema(example = "Hematology")
    public String procedureCategory;

    @Schema(example = "Tests related to blood and blood-forming organs")
    public String categoryDescription;
}




