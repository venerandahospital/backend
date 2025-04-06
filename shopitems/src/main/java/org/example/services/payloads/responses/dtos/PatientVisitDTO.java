package org.example.services.payloads.responses.dtos;

import org.example.domains.PatientVisit;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class PatientVisitDTO {
    public Long id;
    public Long patientId; // New field for patient ID
    public LocalDate visitDate;
    public LocalTime visitTime;
    public String visitType;
    public Integer visitNumber;
    public String visitReason;
    public String visitName;
    public List<ProcedureRequestedDTO> proceduresRequested;
   // public List<TreatmentRequestedDTO> treatmentRequested;
    public List<InitialTriageVitalsDTO> initialTriageVitals;
    public List<VitalsMonitoringChartDTO> vitalsMonitoringChart;
    public List<ConsultationDTO> consultation;
    public List<InvoiceDTO> invoice;
    public List<RecommendationDTO> recommendation;
    public List<InPatientTreatmentDTO> inPatientTreatments;
    public List<ReferralFormDTO> referralForm;

    // Constructor
    public PatientVisitDTO(PatientVisit patientVisit) {
        this.id = patientVisit.id;
        this.patientId = patientVisit.patient != null ? patientVisit.patient.id : null;
        this.visitDate = patientVisit.visitDate;
        this.visitTime = patientVisit.visitTime;
        this.visitType = patientVisit.visitType;
        this.visitNumber = patientVisit.visitNumber;
        this.visitReason = patientVisit.visitReason;
        this.visitName = patientVisit.visitName;

        // Mapping lists with proper null check and stream processing

        /*this.treatmentRequested = patientVisit.getTreatmentRequested() != null ?
                patientVisit.getTreatmentRequested().stream()
                        .map(TreatmentRequestedDTO::new)
                        .collect(Collectors.toList()) : null;*/

        //this.patientId = patientVisit.patient != null ? patientVisit.patient.id : null;


       // this.invoiceId = patientVisit.getInvoice() != null ? patientVisit.getInvoice().id : null;


        this.proceduresRequested = patientVisit.getProceduresRequested() != null ?
                patientVisit.getProceduresRequested().stream()
                        .map(ProcedureRequestedDTO::new)
                        .collect(Collectors.toList()) : null;


        this.initialTriageVitals = patientVisit.getInitialTriageVitals() != null ?
                patientVisit.getInitialTriageVitals().stream()
                        .map(InitialTriageVitalsDTO::new)
                        .collect(Collectors.toList()) : null;


        // this.initialTriageVitals = patientVisit.getInitialTriageVitals().stream().map(InitialTriageVitalsDTO::new).toList();

        this.vitalsMonitoringChart = patientVisit.getVitalsMonitoringChart() != null ?
                patientVisit.getVitalsMonitoringChart().stream()
                        .map(VitalsMonitoringChartDTO::new)
                        .collect(Collectors.toList()) : null;



        this.consultation = patientVisit.getConsultation() != null ?
                patientVisit.getConsultation().stream()
                        .map(ConsultationDTO::new)
                        .collect(Collectors.toList()) : null;


        this.invoice = patientVisit.getInvoice() != null ?
                patientVisit.getInvoice().stream()
                        .map(InvoiceDTO::new)
                        .collect(Collectors.toList()) : null;


        this.recommendation = patientVisit.getRecommendation() != null ?
                patientVisit.getRecommendation().stream()
                        .map(RecommendationDTO::new)
                        .collect(Collectors.toList()) : null;


        this.inPatientTreatments = patientVisit.getInPatientTreatments() != null ?
                patientVisit.getInPatientTreatments().stream()
                        .map(InPatientTreatmentDTO::new)
                        .collect(Collectors.toList()) : null;


        this.referralForm = patientVisit.getReferralForm() != null ?
                patientVisit.getReferralForm().stream()
                        .map(ReferralFormDTO::new)
                        .collect(Collectors.toList()) : null;

    }

    // Getters (for accessing the public fields)
    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }


    public LocalDate getVisitDate() {
        return visitDate;
    }

    public LocalTime getVisitTime() {
        return visitTime;
    }

    public String getVisitType() {
        return visitType;
    }

    public Integer getVisitNumber() {
        return visitNumber;
    }

    public String getVisitReason() {
        return visitReason;
    }

    public String getVisitName() {
        return visitName;
    }

    //public List<TreatmentRequestedDTO> getTreatmentRequested() {
    //    return treatmentRequested;
    //}

    public List<ProcedureRequestedDTO> getProceduresRequested() {
        return proceduresRequested;
    }

    public List<InitialTriageVitalsDTO> getInitialTriageVitals() {
        return initialTriageVitals;
    }

    public List<VitalsMonitoringChartDTO> getVitalsMonitoringChart() {
        return vitalsMonitoringChart;
    }

    public List<ConsultationDTO> getConsultation() {
        return consultation;
    }

    public List<InvoiceDTO> getInvoice() {
        return invoice;
    }

    public List<RecommendationDTO> getRecommendation() {
        return recommendation;
    }

    public List<InPatientTreatmentDTO> getInPatientTreatments() {
        return inPatientTreatments;
    }

    public List<ReferralFormDTO> getReferralForm() {
        return referralForm;
    }
}
