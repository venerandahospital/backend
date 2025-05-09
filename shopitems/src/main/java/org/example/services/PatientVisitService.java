package org.example.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.Patient;
import org.example.domains.PatientVisit;
import org.example.domains.ProcedureRequested;
import org.example.domains.repositories.PatientRepository;
import org.example.domains.repositories.PatientVisitRepository;
import org.example.services.payloads.requests.PatientVisitStatusUpdateRequest;
import org.example.services.payloads.requests.PatientVisitUpdateRequest;
import org.example.services.payloads.requests.PatientVisitRequest;
import org.example.services.payloads.responses.dtos.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class PatientVisitService {

    @Inject
    PatientVisitRepository patientVisitRepository;

    @Inject
    PatientRepository patientRepository;

    @Inject
    InvoiceService invoiceService;

    public static final String NOT_FOUND = "Not found!";

    public Long patientRequestedId;

    /**
     * Creates a new PatientVisit for the specified patient ID.
     */
    @Transactional
    public PatientVisitDTO createNewPatientVisit(Long id, PatientVisitRequest request) {
        // Fetch the patient
        Patient patient = Patient.findById(id);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found for ID: " + id);
        }

        //Generate unique visit number
        int lastVisitNumber = generateVisitNo(id);
        int newVisitNumber = lastVisitNumber + 1;

        // Ensure visitNumber is unique
        boolean exists = patientVisitRepository.find("patient.id = ?1 and visitNumber = ?2", id, newVisitNumber)
                .firstResultOptional()
                .isPresent();
        if (exists) {
            throw new IllegalArgumentException("Duplicate visitNumber: " + newVisitNumber + " for patient ID " + id);
        }

        // Create a new visit
        PatientVisit patientVisit = new PatientVisit();
        patientVisit.visitDate = LocalDate.now();
        patientVisit.visitTime = LocalTime.now();
        patientVisit.visitReason = "Consultation";
        patientVisit.visitStatus = "open";
        patientVisit.visitType = request.visitType;
        patientVisit.visitNumber = newVisitNumber;
        patientVisit.visitName = "Visit 0" + newVisitNumber;
        patientVisit.patient = patient;


        // Persist visit
        patientVisitRepository.persist(patientVisit);

        invoiceService.createInvoice(patientVisit.id);

        return new PatientVisitDTO(patientVisit);
    }
    

    /**
     * Generates the next visit number for a given patient by fetching the highest existing visit number.
     */
    @Transactional
    public int generateVisitNo(Long patientId) {
        return patientVisitRepository.find("patient.id = ?1", Sort.descending("visitNumber"), patientId)
                .list()  // Sort by visit number in descending order
                .stream()
                .map(patientVisit -> patientVisit.visitNumber)  // Map to visit number
                .findFirst()  // Get the highest visit number, if exists
                .orElse(0);  // Return 0 if no visit found
    }
    @Transactional
    public int generateVisitNos(Long patientId) {
        // Query the highest visit number for the given patient
        Integer highestVisitNumber = patientVisitRepository.find("patient.id = ?1", Sort.descending("visitNumber"), patientId)
                .project(Integer.class) // Fetch the `visitNumber` as Integer
                .firstResult(); // Get the first result (highest visit number)

        // If no visits exist, default to 0; otherwise, add 1
        return (highestVisitNumber != null ? highestVisitNumber : 0) + 1;
    }

    @Transactional
    public List<PatientVisitDTO> getAllPatientVisits() {
        // Retrieve all PatientVisit records from the repository
        List<PatientVisit> patientVisits = patientVisitRepository.listAll();

        // Map each PatientVisit to a PatientVisitDTO
        return patientVisits.stream()
                .map(PatientVisitDTO::new)  // Mapping each entity to its DTO
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PatientVisitDTO> getAllPatients() {
        return patientVisitRepository.listAll(Sort.ascending("visitNumber"))
                .stream()
                .map(PatientVisitDTO::new)
                .toList();
    }

    public List<PatientVisitDTO> getVisitByPatientId(Long patientId) {
        // Fetch the list of initial triage vitals by visitId
        List<PatientVisitDTO> result = patientVisitRepository
                .find("patient.id", patientId)
                .list()  // Fetch the result as a List
                .stream()  // Convert the result into a stream for further transformation
                .map(PatientVisitDTO::new)  // Map each entity to a DTO
                .toList();  // Collect the mapped entities into a list

        if (result.isEmpty()) {
            // If no results found, log the error and throw a 404 exception
            String errorMessage = String.format("No patient visits found for patientId: %d", patientId);
            LOG.error(errorMessage);
            throw new WebApplicationException(errorMessage, Response.Status.NOT_FOUND);
        }

        // Return the list of DTOs
        return result;
    }

    @Transactional
    public Response getLatestVisitByPatientId(Long patientId) {
        PatientVisit latestVisit = patientVisitRepository
                .find("patient.id", Sort.descending("id"), patientId)
                .firstResult();

        PatientVisitDTO latestVisitDTO;

        if (latestVisit == null) {
            PatientVisitRequest defaultRequest = new PatientVisitRequest();
            defaultRequest.visitType = "General Consultation";


            // Create a new visit
            latestVisitDTO = createNewPatientVisit(patientId, defaultRequest);
        } else {
            // Convert the found PatientVisit into DTO
            latestVisitDTO = new PatientVisitDTO(latestVisit);
        }

        return Response.ok(new ResponseMessage("Patient visit fetched successfully", latestVisitDTO)).build();
    }





    public PatientVisitDTO updatePatientVisitById(Long id, PatientVisitUpdateRequest request) {
        return patientVisitRepository.findByIdOptional(id)
                .map(patientVisit -> {

                    patientVisit.visitType = request.visitType;
                    patientVisit.visitReason = request.visitReason;
                    patientVisit.visitLastUpdatedDate = LocalDate.now();

                    patientVisitRepository.persist(patientVisit);

                    return new PatientVisitDTO(patientVisit);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public PatientVisitDTO updatePatientVisitStatusById(Long id, PatientVisitStatusUpdateRequest request) {
        return patientVisitRepository.findByIdOptional(id)
                .map(patientVisit -> {

                    patientVisit.visitStatus = request.visitStatus;
                    patientVisit.visitLastUpdatedDate = LocalDate.now();

                    patientVisitRepository.persist(patientVisit);

                    return new PatientVisitDTO(patientVisit);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }






}
