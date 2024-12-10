package org.example.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.domains.*;
import org.example.domains.repositories.InitialTriageVitalsRepository;
import org.example.domains.repositories.PatientVisitRepository;
import org.example.services.payloads.requests.InitialTriageVitalsRequest;
import org.example.services.payloads.requests.PatientVisitRequest;
import org.example.services.payloads.responses.dtos.InitialTriageVitalsDTO;
import org.example.services.payloads.responses.dtos.PatientDTO;
import org.example.services.payloads.responses.dtos.PatientVisitDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class InitialTriageVitalsService {

    @Inject
    InitialTriageVitalsRepository initialTriageVitalsRepository;

    public static final String NOT_FOUND = "Not found!";

    /**
     * Creates a new InitialTriageVitals for the specified patient ID.
     */
    @Transactional
    public InitialTriageVitalsDTO createNewInitialTriageVitals(InitialTriageVitalsRequest request) {
        // Fetch the patient by ID from the database
        PatientVisit patientVisit = PatientVisit.findById(request.visitId);
        if (patientVisit == null) {
            throw new IllegalArgumentException(NOT_FOUND);  // Handle patient not found
        }

        // Create a new PatientVisit object
        InitialTriageVitals initialTriageVitals = new InitialTriageVitals();
        initialTriageVitals.dateTaken = LocalDate.now();
        initialTriageVitals.timeTaken = LocalTime.now();
        initialTriageVitals.spO2 = request.spO2;
        initialTriageVitals.height = request.height;
        initialTriageVitals.heartRate = request.heartRate;
        initialTriageVitals.weight = request.weight;
        initialTriageVitals.temperature = request.temperature;
        initialTriageVitals.bloodPressure = request.bloodPressure;
        initialTriageVitals.respiratoryRate = request.respiratoryRate;

        initialTriageVitals.visit = patientVisit;

        // Persist the new patient visit
        initialTriageVitalsRepository.persist(initialTriageVitals);

        // Convert the PatientVisit entity to a PatientVisitDTO
        return new InitialTriageVitalsDTO(initialTriageVitals);
    }

    @Transactional
    public List<InitialTriageVitalsDTO> getAllInitialTriageVitals() {
        return initialTriageVitalsRepository.listAll(Sort.ascending("id"))
                .stream()
                .map(InitialTriageVitalsDTO::new)
                .toList();
    }


   /* public InitialTriageVitalsDTO getInitialTriageVitalById(Long id) {
        return initialTriageVitalsRepository.findByIdOptional(id)
                .map(InitialTriageVitalsDTO::new)  // Convert InitialTriageVital entity to InitialTriageVitalsDTO
                .orElseThrow(() -> new WebApplicationException("Patient not found", 404));
    }*/

    public List<InitialTriageVitalsDTO> getInitialTriageVitalsByVisitId(Long visitId) {
        // Fetch the list of initial triage vitals by visitId
        List<InitialTriageVitalsDTO> result = initialTriageVitalsRepository
                .find("visit.id", visitId)
                .list()  // Fetch the result as a List
                .stream()  // Convert the result into a stream for further transformation
                .map(InitialTriageVitalsDTO::new)  // Map each entity to a DTO
                .toList();  // Collect the mapped entities into a list

        if (result.isEmpty()) {
            // If no results found, log the error and throw a 404 exception
            String errorMessage = String.format("No InitialTriageVitals found for visitId: %d", visitId);
            LOG.error(errorMessage);
            throw new WebApplicationException(errorMessage, Response.Status.NOT_FOUND);
        }

        // Return the list of DTOs
        return result;
    }



}
