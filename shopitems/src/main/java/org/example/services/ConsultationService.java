package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.domains.Consultation;
import org.example.domains.PatientVisit;
import org.example.domains.repositories.ConsultationRepository;
import org.example.services.payloads.requests.ConsultationRequest;
import org.example.services.payloads.responses.dtos.ConsultationDTO;

import java.math.BigDecimal;

@ApplicationScoped
public class ConsultationService {

    @Inject
    ConsultationRepository consultationRepository;

    private static final String NOT_FOUND = "Not found!";

    public ConsultationDTO createNewConsultation(ConsultationRequest request) {

        PatientVisit patientVisit = PatientVisit.findById(request.visitId);
        if (patientVisit == null) {
            throw new IllegalArgumentException(NOT_FOUND);  // Handle patient not found
        }

        Consultation consultation = new Consultation();
        consultation.consultationFee = BigDecimal.valueOf(10000.00);
        consultation.clinicalExamination = request.clinicalExamination;
        consultation.differentialDiagnosis = request.differentialDiagnosis;
        consultation.diagnosis = request.diagnosis;
        consultation.medicalHistory = request.medicalHistory;
        consultation.visit = patientVisit;

        consultationRepository.persist(consultation);

        return new ConsultationDTO(consultation);


    }

}
