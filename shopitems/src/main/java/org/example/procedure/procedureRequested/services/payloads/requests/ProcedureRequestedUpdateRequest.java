package org.example.procedure.procedureRequested.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ProcedureRequestedUpdateRequest {
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
}
