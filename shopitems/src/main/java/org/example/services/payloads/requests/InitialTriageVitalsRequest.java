package org.example.services.payloads.requests;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.example.domains.PatientVisit;

import java.time.LocalDate;
import java.time.LocalTime;

public class InitialTriageVitalsRequest {

    // Blood Pressure (BP) in mmHg (e.g., 120/80)
    @Schema(example = "120/90")
    public String bloodPressure;  // Blood Pressure in "Systolic/Diastolic" format

    // Body Temperature in Celsius (e.g., 37.2°C)
    @Schema(example = "36")
    public Double temperature;  // Temperature in Celsius

    // Weight of the patient in kilograms (e.g., 70 kg)
    @Schema(example = "70")
    public Double weight;  // Weight in kilograms (kg)

    // Height of the patient in centimeters (e.g., 175 cm)
    @Schema(example = "5.8")
    public Double height;  // Height in centimeters (cm)

    // Oxygen Saturation (SpO2) level (e.g., 98%)
    @Schema(example = "70")
    public Double spO2;  // Oxygen saturation in percentage

    // Heart Rate (HR) in beats per minute (e.g., 75 bpm)
    @Schema(example = "75")
    public Integer heartRate;  // Heart rate in beats per minute (bpm)

    // Respiratory Rate (RR) in breaths per minute (e.g., 18 breaths/min)
    @Schema(example = "18")
    public Integer respiratoryRate;  // Respiratory rate in breaths per minute

    // Foreign key to link initial vitals with a specific patient visit
    @Schema(example = "1")
    public Long visitId;  // The visit the initial vitals belong to
}
