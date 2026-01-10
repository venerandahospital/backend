package org.example.consultations.services.payloads.responses;

import org.example.consultations.domains.Consultation;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ConsultationDTO {
    public Long id;  // The unique identifier of the consultation
    public String medicalHistory;  // A brief medical history of the patient
    public String clinicalExamination;  // Clinical examination findings
    public String differentialDiagnosis;  // List of differential diagnoses
    public String diagnosis;  // Final diagnosis based on the consultation
    public String historyOfPresentingComplaint;  // List of differential diagnoses

    public String chiefComplaint;  // List of differential diagnoses

    public BigDecimal consultationFee;
    public Long visitId;  // The ID of the associated patient visit
    public List<ComplaintDTO> complaints;  // List of complaints associated with this consultation

    public ConsultationDTO(Consultation consultation) {
        this.id = consultation.id;
        this.consultationFee = consultation.consultationFee;
        this.historyOfPresentingComplaint = consultation.historyOfPresentingComplaint;
        this.chiefComplaint = consultation.chiefComplaint;
        this.medicalHistory = consultation.medicalHistory;

        this.clinicalExamination = consultation.clinicalExamination;
        this.differentialDiagnosis = consultation.differentialDiagnosis;
        //this.groupName = patient.patientGroup!= null ? patient.patientGroup.groupNameShortForm : null;

        this.diagnosis = consultation.diagnosis!= null ? consultation.diagnosis : null;
        this.visitId = consultation.visit != null ? consultation.visit.id : null;
        this.complaints = consultation.complaints != null 
            ? consultation.complaints.stream().map(ComplaintDTO::new).collect(Collectors.toList())
            : List.of();
    }
}
