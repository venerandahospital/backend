package org.example.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ConsultationRequest {
    // A brief medical history of the patient (e.g., "Has a history of hypertension, diabetes")
    @Schema(example = "neck surgery")
    public String medicalHistory;

    // Clinical examination findings (e.g., "Normal physical examination", "Swelling in the left ankle")
    @Schema(example = "oral-pharyngeal pain")
    public String clinicalExamination;

    // List of differential diagnoses considered during the consultation (e.g., "Acute Bronchitis, Pneumonia")
    @Schema(example = "pharyngitis, ")
    public String differentialDiagnosis;

    // The final diagnosis made based on the consultation (e.g., "Pneumonia", "Hypertension")
    @Schema(example = "pharyngitis")
    public String diagnosis;

    // One recommendation is associated with one visit
    @Schema(example = "1")
    public Long visitId;  // Link to the specific visit this recommendation is related to

}
