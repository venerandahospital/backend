package org.example.consultations.services.payloads.requests;

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

    @Schema(example = "Doctor")
    public String doneBy;

    @Schema(example = "Done successfully")
    public String report;

    @Schema(example = "Chest Pain")
    public String chiefComplaint;

    @Schema(example = "Chest Pain for 3days")
    public String historyOfPresentingComplaint;

    @Schema(example = "hiv treatment")
    public String medicationHistory;

    @Schema(example = "uticaria")
    public String allergies;

    @Schema(example = "hypertensive history")
    public String familyHistory;

    @Schema(example = "smoking history")
    public String socialHistory;

    @Schema(example = "normal heart beat")
    public String systemicExamination;

    @Schema(example = "normal heart beat")
    public String clinicalImpression;

    @Schema(example = "normal heart beat")
    public String followUpInstructions;

    @Schema(example = "normal heart beat")
    public String notes;




















}
