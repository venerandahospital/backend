package org.example.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;


public class PatientVisitRequest {


    @Schema(example = "admission")
    public String visitType; // e.g., "outpatient or inpatient"

    @Schema(example = "Review")
    public String visitReason;

    //////////////////////////// Medical Details ///////////////////////////////



}
