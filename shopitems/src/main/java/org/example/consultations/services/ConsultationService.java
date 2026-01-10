package org.example.consultations.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.domains.Complaint;
import org.example.consultations.domains.ComplaintRepository;
import org.example.consultations.domains.ComplaintSite;
import org.example.consultations.domains.ComplaintSiteRepository;
import org.example.consultations.domains.ComplaintType;
import org.example.consultations.domains.ComplaintTypeRepository;
import org.example.consultations.domains.Consultation;
import org.example.consultations.domains.ConsultationRepository;
import org.example.consultations.services.payloads.requests.ComplaintRequest;
import org.example.consultations.services.payloads.requests.ConsultationRequest;
import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.procedure.itemUsedInProcedure.services.ItemUsedService;
import org.example.procedure.procedure.domains.Procedure;
import org.example.procedure.procedure.domains.repositories.ProcedureRepository;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ConsultationService {

    @Inject
    ConsultationRepository consultationRepository;

    @Inject
    ComplaintRepository complaintRepository;

    @Inject
    ComplaintTypeRepository complaintTypeRepository;

    @Inject
    ComplaintSiteRepository complaintSiteRepository;

    @Inject
    ProcedureRepository procedureRepository;

    @Inject
    ItemUsedService itemUsedService;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;

    private static final String NOT_FOUND = "Not found!";

    private void processComplaints(Consultation consultation, List<ComplaintRequest> complaintRequests) {
        if (complaintRequests == null || complaintRequests.isEmpty()) {
            return;
        }

        // Clear existing complaints if updating
        if (consultation.complaints != null && !consultation.complaints.isEmpty()) {
            consultation.complaints.clear();
        }

        // Create new complaints from request
        for (ComplaintRequest complaintRequest : complaintRequests) {
            Complaint complaint = new Complaint();
            complaint.consultation = consultation;
            
            // Fetch ComplaintSite by ID
            if (complaintRequest.siteId != null) {
                ComplaintSite complaintSite = complaintSiteRepository.findById(complaintRequest.siteId);
                if (complaintSite == null) {
                    throw new WebApplicationException("ComplaintSite with id " + complaintRequest.siteId + " not found", Response.Status.BAD_REQUEST);
                }
                complaint.site = complaintSite;
            } else {
                throw new WebApplicationException("ComplaintSite ID is required", Response.Status.BAD_REQUEST);
            }
            
            // Fetch ComplaintType by ID
            if (complaintRequest.typeId != null) {
                ComplaintType complaintType = complaintTypeRepository.findById(complaintRequest.typeId);
                if (complaintType == null) {
                    throw new WebApplicationException("ComplaintType with id " + complaintRequest.typeId + " not found", Response.Status.BAD_REQUEST);
                }
                complaint.type = complaintType;
            } else {
                throw new WebApplicationException("ComplaintType ID is required", Response.Status.BAD_REQUEST);
            }
            
            complaint.duration = complaintRequest.duration;
            complaint.natureCharacter = complaintRequest.natureCharacter;
            complaint.severity = complaintRequest.severity;
            complaint.onset = complaintRequest.onset;
            complaint.courseProgression = complaintRequest.courseProgression;
            complaint.aggravatingFactors = complaintRequest.aggravatingFactors;
            complaint.relievingFactors = complaintRequest.relievingFactors;
            complaint.associatedSymptoms = complaintRequest.associatedSymptoms;
            complaint.creationDate = LocalDate.now();
            
            consultation.complaints.add(complaint);
        }
    }

    @Transactional
    public Response createNewConsultation(Long visitId, ConsultationRequest request) {
        // Fetch the patient visit by ID
        PatientVisit patientVisit = PatientVisit.findById(visitId);

        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        Procedure procedure = procedureRepository.findByCategory("consultation");

        ProcedureRequested procedureRequested = new ProcedureRequested();

        Consultation consultation = new Consultation();

        boolean consultationAlreadyDone = procedureRequestedRepository
                .find("category = ?1 and visit.id = ?2", "consultation", visitId)
                .firstResultOptional()
                .isPresent();


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

            // Process complaints
            processComplaints(existingConsultation, request.complaints);

            // Save consultation
            consultationRepository.persist(existingConsultation);



            if ("consultation".equalsIgnoreCase(procedure.category.name)) {


                if (consultationAlreadyDone) {


                    //update notes


                    return Response.status(Response.Status.CONFLICT)
                            .entity(new ResponseMessage("Everything Already done, Clinical notes already saved and Consultation fee already billed for this visit", new ConsultationDTO(existingConsultation)))
                            .build();
                }else{



                    itemUsedService.performProcedure(procedure.id);


                    return Response.status(Response.Status.CONFLICT)
                            .entity(new ResponseMessage("Clinical notes already saved and Consultation fee billed successfully for this visit", new ConsultationDTO(existingConsultation)))
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

            // Process complaints
            processComplaints(consultation, request.complaints);

            // Save consultation
            consultationRepository.persist(consultation);

            // Get procedure by category

            // Check if it's a consultation category
            if ("consultation".equalsIgnoreCase(procedure.category.name)) {

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

        if ("closed".equals(patientVisit.visitStatus)) {
            throw new WebApplicationException("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848:",409);

        }


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