package org.example.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.*;
import org.example.domains.repositories.InitialTriageVitalsRepository;
import org.example.domains.repositories.PatientVisitRepository;
import org.example.services.payloads.requests.InitialTriageVitalsRequest;
import org.example.services.payloads.requests.InitialVitalUpdateRequest;
import org.example.services.payloads.requests.PatientUpdateRequest;
import org.example.services.payloads.requests.PatientVisitRequest;
import org.example.services.payloads.responses.dtos.InitialTriageVitalsDTO;
import org.example.services.payloads.responses.dtos.PatientDTO;
import org.example.services.payloads.responses.dtos.PatientVisitDTO;
import org.example.services.payloads.responses.dtos.ProcedureRequestedDTO;

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
    public InitialTriageVitalsDTO createNewInitialTriageVitals(Long id, InitialTriageVitalsRequest request) {
        // Fetch the patient by ID from the database
        PatientVisit patientVisit = PatientVisit.findById(id);
        if (patientVisit == null) {
            throw new IllegalArgumentException(NOT_FOUND);  // Handle patient not found
        }

        // Create a new InitialTriageVitals object
        InitialTriageVitals initialTriageVitals = new InitialTriageVitals();
        initialTriageVitals.dateTaken = request.dateTaken;
        initialTriageVitals.timeTaken = request.timeTaken;
        initialTriageVitals.spO2 = request.spO2;
        initialTriageVitals.station = request.station;
        initialTriageVitals.height = request.height;
        initialTriageVitals.heartRate = request.heartRate;
        initialTriageVitals.weight = request.weight;
        initialTriageVitals.temperature = request.temperature;
        initialTriageVitals.systolic = request.systolic;
        initialTriageVitals.diastolic = request.diastolic;
        initialTriageVitals.pulseRate = request.pulseRate;
        initialTriageVitals.takenBy = request.takenBy;

        // Calculate Mean Arterial Pressure (MAP)
        initialTriageVitals.map = request.diastolic + (request.systolic - request.diastolic) / 3.0;

        initialTriageVitals.respiratoryRate = request.respiratoryRate;
        initialTriageVitals.bloodPressure = request.systolic + "/" + request.diastolic;

        initialTriageVitals.visit = patientVisit;

        // Persist the new initial triage vitals
        initialTriageVitalsRepository.persist(initialTriageVitals);

        // Convert the InitialTriageVitals entity to an InitialTriageVitalsDTO
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
        // Query for ProcedureRequested where procedureRequestedType is "LabTest" and visit ID matches, ordered descending
        List<InitialTriageVitals> initialTriageVitals = InitialTriageVitals.find(
                "visit.id = ?1 ORDER BY id DESC", // Replace 'id' with your desired field for sorting
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return initialTriageVitals.stream()
                .map(InitialTriageVitalsDTO::new)
                .toList();
    }

    public InitialTriageVitalsDTO updateInitialVitalById(Long id, InitialVitalUpdateRequest request) {

        return initialTriageVitalsRepository.findByIdOptional(id)
                .map(initialTriageVitals -> {

                    initialTriageVitals.dateTaken = request.dateTaken;
                    initialTriageVitals.timeTaken = request.timeTaken;
                    initialTriageVitals.spO2 = request.spO2;
                    initialTriageVitals.station = request.station;
                    initialTriageVitals.height = request.height;
                    initialTriageVitals.heartRate = request.heartRate;
                    initialTriageVitals.weight = request.weight;
                    initialTriageVitals.temperature = request.temperature;
                    initialTriageVitals.systolic = request.systolic;
                    initialTriageVitals.diastolic = request.diastolic;
                    initialTriageVitals.pulseRate = request.pulseRate;
                    initialTriageVitals.takenBy = request.takenBy;

                    // Calculate Mean Arterial Pressure (MAP)
                    initialTriageVitals.map = request.diastolic + (request.systolic - request.diastolic) / 3.0;

                    initialTriageVitals.respiratoryRate = request.respiratoryRate;
                    initialTriageVitals.bloodPressure = request.systolic + "/" + request.diastolic;

                    initialTriageVitalsRepository.persist(initialTriageVitals);

                    return new InitialTriageVitalsDTO(initialTriageVitals);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    @Transactional
    public Response deleteVitalById(Long id) {

        InitialTriageVitals initialTriageVitals = initialTriageVitalsRepository.findById(id);

        if (initialTriageVitals == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        initialTriageVitalsRepository.delete(initialTriageVitals);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }





























}
