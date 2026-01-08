package org.example.procedure.procedure.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ProcedureTypeRequest {
    @Schema(example = "labtest")
    public String procedureType;

    @Schema(example = "Laboratory tests performed on samples")
    public String typeDescription;
}




