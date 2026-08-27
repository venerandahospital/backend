package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class DiagnosisTypeRequest {
    @Schema(example = "Pneumonia", description = "Title of the diagnosis type")
    public String title;

    @Schema(example = "Lower respiratory tract infection", description = "Optional description")
    public String description;
}
