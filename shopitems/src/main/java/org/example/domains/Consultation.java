package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
public class Consultation extends PanacheEntity {

    // A brief medical history of the patient (e.g., "Has a history of hypertension, diabetes")
    @Column(columnDefinition = "TEXT")
    public String medicalHistory;

    // Clinical examination findings (e.g., "Normal physical examination", "Swelling in the left ankle")
    @Column(columnDefinition = "TEXT")
    public String clinicalExamination;

    // List of differential diagnoses considered during the consultation (e.g., "Acute Bronchitis, Pneumonia")
    @Column(columnDefinition = "TEXT")
    public String differentialDiagnosis;

    // The final diagnosis made based on the consultation (e.g., "Pneumonia", "Hypertension")
    @Column(columnDefinition = "TEXT")
    public String diagnosis;

    // One recommendation is associated with one visit
    @OneToOne
    @JoinColumn(nullable = false)  // Foreign key to link to the PatientVisit
    public PatientVisit visit;  // Link to the specific visit this recommendation is related to

    // You can add other fields as needed to capture consultation details
}
