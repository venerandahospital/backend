package org.example.lab.singleStatementReport.malaria.services.Payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;

public class MalariaUpdateRequest {



    @Schema(example = "bs and mrdt")
    public String test;

    @Schema(example = "Reactive")
    public String bs;

    @Schema(example = "Reactive")
    public String mrdt;

    @Schema(example = "weakly reactive")
    public String notes;

    @Schema(example = "clyton")
    public String patientName;

    @Schema(example = "male")
    public String gender;

    @Schema(example = "40")
    public BigDecimal patientAge;

    @Schema(example = "Lap")
    public String recommendation;


    @Schema(example = "Mr. Muwanguzi Collin")
    public String doneBy;


}
