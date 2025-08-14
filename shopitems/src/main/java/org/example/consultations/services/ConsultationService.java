package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.Consultation;
import org.example.consultations.domains.ConsultationRepository;
import org.example.consultations.services.payloads.requests.ConsultationRequest;
import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.procedure.itemUsedInProcedure.services.ItemUsedService;
import org.example.procedure.procedure.domains.Procedure;
import org.example.procedure.procedure.domains.repositories.ProcedureRepository;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;
import org.example.vitals.domains.InitialTriageVitals;
import org.example.vitals.services.payloads.responses.InitialTriageVitalsDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ConsultationService {

    @Inject
    ConsultationRepository consultationRepository;

    @Inject
    ProcedureRepository procedureRepository;

    @Inject
    ItemUsedService itemUsedService;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;

    private static final String NOT_FOUND = "Not found!";

    @Transactional
    public Response createNewConsultation(Long visitId, ConsultationRequest request) {
        // Fetch the patient visit by ID
        PatientVisit patientVisit = PatientVisit.findById(visitId);

        Procedure procedure = procedureRepository.findByCategory("consultation");

        ProcedureRequested procedureRequested = new ProcedureRequested();

        Consultation consultation = new Consultation();

        boolean consultationAlreadyDone = procedureRequestedRepository
                .find("category = ?1 and visit.id = ?2", "consultation", visitId)
                .firstResultOptional()
                .isPresent();





        if (patientVisit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Patient visit not found for ID: " + visitId, null))
                    .build();
        }

        // Check if a consultation already exists for this visit
        Consultation existingConsultation = consultationRepository.find("visit.id", visitId).firstResult();
        if (existingConsultation != null) {


            // Create a new consultation if none exists
            existingConsultation.visit = patientVisit;
            existingConsultation.doneBy = request.doneBy;
            existingConsultation.historyOfPresentingComplaint = request.historyOfPresentingComplaint;

            existingConsultation.chiefComplaint = request.chiefComplaint;
            existingConsultation.report = request.report;

            existingConsultation.allergies = request.allergies;
            existingConsultation.familyHistory = request.familyHistory;
            existingConsultation.socialHistory = request.socialHistory;
            existingConsultation.systemicExamination = request.systemicExamination;
            existingConsultation.clinicalImpression = request.clinicalImpression;
            existingConsultation.followUpInstructions = request.followUpInstructions;
            existingConsultation.notes = request.notes;

            existingConsultation.medicationHistory = request.medicationHistory;

            existingConsultation.clinicalExamination = request.clinicalExamination;
            existingConsultation.differentialDiagnosis = request.differentialDiagnosis;
            existingConsultation.diagnosis = request.diagnosis;
            existingConsultation.updateDate = LocalDate.now();
            existingConsultation.medicalHistory = request.medicalHistory;

            // Save consultation
            consultationRepository.persist(existingConsultation);



            if ("consultation".equalsIgnoreCase(procedure.category)) {


                if (consultationAlreadyDone) {


                    //update notes


                    return Response.status(Response.Status.CONFLICT)
                            .entity(new ResponseMessage("Everything Already done, Clinical notes already saved and Consultation fee already billed for this visit", new ConsultationDTO(consultation)))
                            .build();
                }else{



                    itemUsedService.performProcedure(procedure.id);


                    return Response.status(Response.Status.CONFLICT)
                            .entity(new ResponseMessage("Clinical notes already saved and Consultation fee billed successfully for this visit", new ConsultationDTO(consultation)))
                            .build();
                }
            }


        }else{
            // Create a new consultation if none exists
            consultation.visit = patientVisit;
            consultation.chiefComplaint = request.chiefComplaint;
            consultation.doneBy = request.doneBy;
            consultation.historyOfPresentingComplaint = request.historyOfPresentingComplaint;
            consultation.medicationHistory = request.medicationHistory;
            consultation.allergies = request.allergies;
            consultation.familyHistory = request.familyHistory;
            consultation.socialHistory = request.socialHistory;
            consultation.systemicExamination = request.systemicExamination;

            consultation.clinicalImpression = request.clinicalImpression;

            consultation.followUpInstructions = request.followUpInstructions;

            consultation.notes = request.notes;




            consultation.report = request.report;
            consultation.clinicalExamination = request.clinicalExamination;
            consultation.differentialDiagnosis = request.differentialDiagnosis;
            consultation.diagnosis = request.diagnosis;
            consultation.creationDate = LocalDate.now();
            consultation.medicalHistory = request.medicalHistory;

            // Save consultation
            consultationRepository.persist(consultation);

            // Get procedure by category

            // Check if it's a consultation category
            if ("consultation".equalsIgnoreCase(procedure.category)) {

                if (consultationAlreadyDone) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(new ResponseMessage("Clinical notes saved successfully, Consultation fee already billed before for this visit", new ConsultationDTO(consultation)))
                            .build();
                }else{



                    itemUsedService.performProcedure(procedure.id);



                    return Response.status(Response.Status.CREATED)
                            .entity(new ResponseMessage("Clinical notes saved successfully, Consultation fee saved successfully for this visit", new ConsultationDTO(consultation)))
                            .build();
                }
            }
        }

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient consultation saved successfully", new ConsultationDTO(consultation)))
                .build();
    }

    @Transactional
    public ConsultationDTO newConsultationOnTheGo(Long visitId){

        PatientVisit patientVisit = PatientVisit.findById(visitId);

        Consultation consultation = new Consultation();


        consultation.visit = patientVisit;
        consultation.chiefComplaint = "please input presenting complaint";
        consultation.doneBy = "system";
        consultation.historyOfPresentingComplaint = "historyOfPresentingComplaint";
        consultation.medicationHistory = "medicationHistory";
        consultation.allergies = "allergies";
        consultation.familyHistory = "familyHistory";
        consultation.socialHistory = "socialHistory";
        consultation.systemicExamination = "systemicExamination";
        consultation.clinicalImpression = "clinicalImpression";
        consultation.followUpInstructions = "followUpInstructions";
        consultation.notes = "notes";
        consultation.report = "report";
        consultation.clinicalExamination = "clinicalExamination";
        consultation.differentialDiagnosis = "differentialDiagnosis";
        consultation.diagnosis = "please type diagnosis";
        consultation.creationDate = LocalDate.now();
        consultation.medicalHistory = "medicalHistory";


        consultationRepository.persist(consultation);

        return new ConsultationDTO(consultation);

    }




    @Transactional
    public ConsultationDTO getFirstConsultationByVisitId(Long visitId) {
        Consultation consultationNew = Consultation.find(
                "visit.id = ?1 ORDER BY id DESC", visitId
        ).firstResult();


        if (consultationNew == null) {

            return newConsultationOnTheGo(visitId);

        }else {
            return new ConsultationDTO(consultationNew);
        }

        //return consultation != null ? new ConsultationDTO(consultation) : null;


    }
    @Transactional
    public ConsultationDTO getFirstConsultationByVisitIdReturnConsultation(Long visitId) {
        Consultation consultation = Consultation.find(
                "visit.id = ?1 ORDER BY id DESC", visitId
        ).firstResult();



        return  new ConsultationDTO(consultation);
    }

    public List<ConsultationDTO> getAllConsultations() {
        return consultationRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ConsultationDTO::new)
                .toList();
    }












}