package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class InitialTriageVitals extends PanacheEntity {

    // Date when the treatment was given
    @Column(nullable = false)
    public LocalDate dateTaken;

    // Time when the treatment was administered
    @Column(nullable = false)
    public LocalTime timeTaken;

    // Blood Pressure (BP) in mmHg (e.g., 120/80)
    @Column(nullable = false)
    public String bloodPressure;  // Blood Pressure in "Systolic/Diastolic" format

    // Body Temperature in Celsius (e.g., 37.2°C)
    @Column(nullable = false)
    public Double temperature;  // Temperature in Celsius

    // Weight of the patient in kilograms (e.g., 70 kg)
    @Column(nullable = false)
    public Double weight;  // Weight in kilograms (kg)

    // Height of the patient in centimeters (e.g., 175 cm)
    @Column(nullable = false)
    public Double height;  // Height in centimeters (cm)

    // Oxygen Saturation (SpO2) level (e.g., 98%)
    @Column(nullable = false)
    public Double spO2;  // Oxygen saturation in percentage

    // Heart Rate (HR) in beats per minute (e.g., 75 bpm)
    @Column(nullable = false)
    public Integer heartRate;  // Heart rate in beats per minute (bpm)

    // Respiratory Rate (RR) in breaths per minute (e.g., 18 breaths/min)
    @Column(nullable = false)
    public Integer respiratoryRate;  // Respiratory rate in breaths per minute

    // Foreign key to link initial vitals with a specific patient visit
    @ManyToOne
    @JoinColumn(nullable = false)
    public PatientVisit visit;  // The visit the initial vitals belong to
}
