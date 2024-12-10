package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PatientVisit extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    public Patient patient;

    @Column
    @JsonbDateFormat(value = "dd/MM/yyyy")
    public LocalDate visitDate;

    @Column
    public LocalTime visitTime;  // Stores the time of the visit

    @Column
    public String visitType; // e.g., "outpatient or inpatient"

    @Column(nullable = false)
    public int visitNumber;  // A unique identifier for each visit within a patient’s records

    @Column
    public String visitReason;

    @Column
    public String visitName;

    //////////////////////////// Medical Details ///////////////////////////////

    @OneToMany(mappedBy = "visit", fetch = FetchType.EAGER)
    private List<TreatmentRequested> treatmentRequested = new ArrayList<>(); // Private list

    @OneToMany(mappedBy = "visit", fetch = FetchType.EAGER)
    private List<ProcedureRequested> ProceduresRequested = new ArrayList<>(); // Private list

    @OneToMany(mappedBy = "visit", fetch = FetchType.EAGER)
    private List<InitialTriageVitals> initialTriageVitals = new ArrayList<>();

    @OneToMany(mappedBy = "visit", fetch = FetchType.EAGER)
    private List<VitalsMonitoringChart> vitalsMonitoringChart = new ArrayList<>();  // Private list

    @OneToOne(mappedBy = "visit", fetch = FetchType.EAGER)  // One-to-one relationship
    private Consultation consultation;  // A single Consultation for the visit

    @OneToOne(mappedBy = "visit", fetch = FetchType.EAGER)  // One-to-one relationship
    private Recommendation recommendation;  // A single recommendation for the visit

    @OneToMany(mappedBy = "visit", fetch = FetchType.EAGER)
    private List<InPatientTreatment> inPatientTreatments = new ArrayList<>();  // Private list

    @OneToOne(mappedBy = "visit", fetch = FetchType.EAGER)  // One-to-one relationship
    private ReferralForm referralForm;  // A single referral form for the visit

    // Getters and Setters for private fields


    public List<TreatmentRequested> getTreatmentRequested() {
        return treatmentRequested;
    }

    public void setTreatmentRequested(List<TreatmentRequested> treatmentRequested) {
        this.treatmentRequested = treatmentRequested;
    }

    public List<ProcedureRequested> getProceduresRequested() {
        return ProceduresRequested;
    }

    public void setProceduresRequested(List<ProcedureRequested> proceduresRequested) {
        this.ProceduresRequested = proceduresRequested;
    }

    public List<InitialTriageVitals> getInitialTriageVitals() {
        return initialTriageVitals;
    }

    public void setInitialTriageVitals(List<InitialTriageVitals> initialTriageVitals) {
        this.initialTriageVitals = initialTriageVitals;
    }

    public List<VitalsMonitoringChart> getVitalsMonitoringChart() {
        return vitalsMonitoringChart;
    }

    public void setVitalsMonitoringChart(List<VitalsMonitoringChart> vitalsMonitoringChart) {
        this.vitalsMonitoringChart = vitalsMonitoringChart;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    public List<InPatientTreatment> getInPatientTreatments() {
        return inPatientTreatments;
    }

    public void setInPatientTreatments(List<InPatientTreatment> inPatientTreatments) {
        this.inPatientTreatments = inPatientTreatments;
    }

    public ReferralForm getReferralForm() {
        return referralForm;
    }

    public void setReferralForm(ReferralForm referralForm) {
        this.referralForm = referralForm;
    }

    public Patient getPatient() {
        return patient;
    }

    // Setter for patient (optional, depending on your needs)
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}
