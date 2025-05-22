package org.example.visit;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class PatientVisitUpdateRequest {

    @Schema(example = "admission")
    public String visitType; // e.g., "outpatient or inpatient"

    @Schema(example = "Review")
    public String visitReason;
}
