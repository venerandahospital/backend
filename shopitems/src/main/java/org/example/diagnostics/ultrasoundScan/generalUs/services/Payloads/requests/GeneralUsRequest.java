package org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;


public class GeneralUsRequest {

    @Schema(example = "1")
    public Long visitId;

    @Schema(example = "2")
    public Long procedureRequestedId;

    @Schema(example = "Lap and Abdominal Distention")
    public String indication;

    @Schema(example = "1")
    public Long patientId;

    @Schema(example = "Mr.Muwanguzi Joel")
    public String doneBy;

    @Schema(example = "Abdominal Scan")
    public String exam;

    @Schema(example = "The urinary bladder is well distended with thick wall thickness and internal echoes noted")
    public String findings;

    @Schema(example = "Mild organomegaly")
    public String impression;

    @Schema(example = "Urinalysis recommended")
    public String recommendation;


}
