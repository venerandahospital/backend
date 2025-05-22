package org.example.visit;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class PatientVisitStatusUpdateRequest {
    @Schema(example = "open")
    public String visitStatus;
}
