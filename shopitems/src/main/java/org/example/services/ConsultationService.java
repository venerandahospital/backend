package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.Consultation;
import org.example.domains.PatientVisit;
import org.example.domains.repositories.ConsultationRepository;
import org.example.services.payloads.requests.ConsultationRequest;
import org.example.services.payloads.responses.dtos.ConsultationDTO;
import org.example.services.payloads.responses.dtos.InitialTriageVitalsDTO;

import java.math.BigDecimal;

@ApplicationScoped
public class ConsultationService {

    @Inject
    ConsultationRepository consultationRepository;

    private static final String NOT_FOUND = "Not found!";

    @Transactional
    public Response createNewConsultation(Long visitId, ConsultationRequest request) {
        // Fetch the patient visit by ID
        PatientVisit patientVisit = PatientVisit.findById(visitId);
        if (patientVisit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Patient visit not found for ID: " + visitId, null))
                    .build();
        }

        // Check if a consultation already exists for this visit
        Consultation existingConsultation = Consultation.find("visit.id", visitId).firstResult();
        if (existingConsultation != null) {
            return Response.status(Response.Status.CONFLICT) // HTTP 409 Conflict
                    .entity(new ResponseMessage("A consultation already exists for this visit ID: " + visitId, null))
                    .build();
        }

        // Create a new consultation if none exists
        Consultation consultation = new Consultation();
        consultation.consultationFee = BigDecimal.valueOf(10000.00);
        consultation.clinicalExamination = request.clinicalExamination;
        consultation.differentialDiagnosis = request.differentialDiagnosis;
        consultation.diagnosis = request.diagnosis;
        consultation.medicalHistory = request.medicalHistory;
        consultation.visit = patientVisit;

        // Persist the new consultation
        consultationRepository.persist(consultation);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient consultation saved successfully", new ConsultationDTO(consultation)))
                .build();
    }


}
