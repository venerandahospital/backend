package org.example.consultations.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class DiagnosisRequest {

    @Schema(example = "Pneumonia", description = "Diagnosis name")
    public String name;

    @Schema(example = "Severe", description = "Severity, e.g. Mild, Moderate, Severe")
    public String severity;

    @Schema(example = "final", description = "Kind: final or differential")
    public String kind;

    @Schema(example = "Community acquired", description = "Optional clinical notes")
    public String notes;

    @Schema(description = "Existing diagnosis id when updating via consultation sync")
    public Long id;
}
