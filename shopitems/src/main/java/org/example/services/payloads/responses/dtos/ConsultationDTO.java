package org.example.services.payloads.responses.dtos;

import org.example.domains.Consultation;

public class ConsultationDTO {
    public Long id;  // The unique identifier of the consultation
    public String medicalHistory;  // A brief medical history of the patient
    public String clinicalExamination;  // Clinical examination findings
    public String differentialDiagnosis;  // List of differential diagnoses
    public String diagnosis;  // Final diagnosis based on the consultation
    public Long visitId;  // The ID of the associated patient visit

    public ConsultationDTO(Consultation consultation) {
        this.id = consultation.id;
        this.medicalHistory = consultation.medicalHistory;
        this.clinicalExamination = consultation.clinicalExamination;
        this.differentialDiagnosis = consultation.differentialDiagnosis;
        this.diagnosis = consultation.diagnosis;
        this.visitId = consultation.visit != null ? consultation.visit.id : null;
    }
}
