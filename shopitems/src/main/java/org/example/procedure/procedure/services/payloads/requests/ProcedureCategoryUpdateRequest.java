package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ProcedureCategoryUpdateRequest {

    @Schema(example = "labTest")
    public String procedureCategory;

    @Schema(example = "for laTest")
    public String categoryDescription;
}
