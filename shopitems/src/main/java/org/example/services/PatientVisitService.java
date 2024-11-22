package org.example.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.domains.Patient;
import org.example.domains.PatientVisit;
import org.example.domains.repositories.PatientVisitRepository;
import org.example.services.payloads.requests.PatientVisitRequest;
import org.example.services.payloads.responses.dtos.PatientDTO;
import org.example.services.payloads.responses.dtos.PatientVisitDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PatientVisitService {

    @Inject
    PatientVisitRepository patientVisitRepository;

    public static final String NOT_FOUND = "Not found!";

    /**
     * Creates a new PatientVisit for the specified patient ID.
     */
    @Transactional
    public PatientVisitDTO createNewPatientVisit(PatientVisitRequest request) {
        // Fetch the patient by ID from the database
        Patient patient = Patient.findById(request.patientId);
        if (patient == null) {
            throw new IllegalArgumentException(NOT_FOUND);  // Handle patient not found
        }

        // Create a new PatientVisit object
        PatientVisit patientVisit = new PatientVisit();
        patientVisit.visitDate = LocalDate.now();
        patientVisit.visitTime = LocalTime.now();
        patientVisit.visitReason = request.visitReason;
        patientVisit.visitType = request.visitType;

        // Generate the visit number
        int lastVisitNumber = generateVisitNos(request.patientId);
        patientVisit.visitNumber = lastVisitNumber + 1;  // Generate next visit number
        patientVisit.visitName = "Visit " + patientVisit.visitNumber;
        patientVisit.patient = patient;

        // Persist the new patient visit
        patientVisitRepository.persist(patientVisit);

        // Convert the PatientVisit entity to a PatientVisitDTO
        return new PatientVisitDTO(patientVisit);
    }


    /**
     * Generates the next visit number for a given patient by fetching the highest existing visit number.
     */
    @Transactional
    public int generateVisitNo(Long patientId) {
        return patientVisitRepository.find("patient.id", patientId)
                .list()  // Sort by visit number in descending order
                .stream()
                .map(patientVisit -> patientVisit.visitNumber)  // Map to visit number
                .findFirst()  // Get the highest visit number, if exists
                .orElse(0);  // Return 0 if no visit found
    }
    @Transactional
    public int generateVisitNos(Long patientId) {
        return patientVisitRepository.find("patient.id", Sort.descending("visitNumber"), patientId)  // Sort by visit number in descending order
                .list()  // Retrieve the sorted list
                .stream()
                .map(patientVisit -> patientVisit.visitNumber)  // Map to visit number
                .findFirst()  // Get the highest visit number, if exists
                .orElse(0);  // Return 0 if no visit found
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



}
