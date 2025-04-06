package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.InitialTriageVitals;
import org.example.domains.Procedure;
import org.example.domains.ProcedureRequested;
import org.example.domains.PatientVisit;
import org.example.domains.repositories.ProcedureRequestedRepository;
import org.example.services.payloads.requests.InitialVitalUpdateRequest;
import org.example.services.payloads.requests.ProcedureRequestedRequest;
import org.example.services.payloads.requests.ProcedureRequestedUpdateRequest;
import org.example.services.payloads.responses.dtos.InitialTriageVitalsDTO;
import org.example.services.payloads.responses.dtos.PatientDTO;
import org.example.services.payloads.responses.dtos.ProcedureRequestedDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ProcedureRequestedService {
    @Inject
    ProcedureRequestedRepository proceduresRequestedRepository;

    public static final String NOT_FOUND = "Not found!";

    public ProcedureRequestedDTO createNewProcedureRequested(Long visitID, ProcedureRequestedRequest request) {
        // Fetch the PatientVisit and Procedure in one go
        PatientVisit patientVisit = PatientVisit.findById(visitID);

        Procedure procedure = Procedure.findById(request.procedureId);

        // Throw exception if either of them is not found
        if (patientVisit == null || procedure == null) {
            throw new IllegalArgumentException(NOT_FOUND); // Handle not found error
        }

        // Check if a ProcedureRequested with the same procedure and visit already exists
        ProcedureRequested existingProcedureRequested = ProcedureRequested.find(
                "visit.id = ?1 and procedure.id = ?2",
                patientVisit.id,
                procedure.id
        ).firstResult();

        if (existingProcedureRequested != null) {

            // If it exists, increment the quantity and update the total amount
            existingProcedureRequested.quantity += 1;
            existingProcedureRequested.totalAmount = BigDecimal.valueOf(existingProcedureRequested.quantity)
                    .multiply(existingProcedureRequested.unitSellingPrice);

            proceduresRequestedRepository.persist(existingProcedureRequested); // Persist the updated entity

            return new ProcedureRequestedDTO(existingProcedureRequested);
        } else {
            // Otherwise, create a new ProcedureRequested record
            ProcedureRequested procedureRequested = new ProcedureRequested();
            procedureRequested.quantity = request.quantity;
            procedureRequested.report = request.report;
            procedureRequested.orderedBy = request.orderedBy;
            procedureRequested.doneBy = request.doneBy;
            procedureRequested.unitSellingPrice = procedure.unitSellingPrice;
            procedureRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(procedure.unitSellingPrice);
            procedureRequested.visit = patientVisit;
            procedureRequested.procedure = procedure;
            procedureRequested.procedureRequestedType = procedure.category;
            procedureRequested.dateOfProcedure = java.time.LocalDate.now();
            procedureRequested.timeOfProcedure = java.time.LocalTime.now();

            proceduresRequestedRepository.persist(procedureRequested);
            return new ProcedureRequestedDTO(procedureRequested);
        }
    }

    public ProcedureRequestedDTO getOtherRequestedProcedureById(Long id) {
        return proceduresRequestedRepository.findByIdOptional(id)
                .map(ProcedureRequestedDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("ProcedureRequested not found", 404));
    }


    public ProcedureRequestedDTO updateProcedureRequestedById(Long id, ProcedureRequestedUpdateRequest request) {
        Procedure procedure = Procedure.findById(request.procedureId);
        return proceduresRequestedRepository.findByIdOptional(id)
                .map(procedureRequested -> {

                    procedureRequested.doneBy = request.doneBy;
                    procedureRequested.orderedBy = request.orderedBy;
                    procedureRequested.report = request.report;
                    procedureRequested.procedure = procedure;
                    procedureRequested.quantity = request.quantity;
                    procedureRequested.unitSellingPrice = procedure.unitSellingPrice;
                    procedureRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(procedure.unitSellingPrice);
                    procedureRequested.procedureRequestedType = procedure.procedureType;
                    procedureRequested.updateDate = LocalDate.now();

                    proceduresRequestedRepository.persist(procedureRequested);

                    return new ProcedureRequestedDTO(procedureRequested);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }


    public List<ProcedureRequestedDTO> getLabTestProceduresByVisit(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is "LabTest" and visit ID matches, ordered descending
        List<ProcedureRequested> labTestProcedures = ProcedureRequested.find(
                "procedureRequestedType = ?1 and visit.id = ?2 ORDER BY id DESC", // Replace 'id' with your desired field for sorting
                "LabTest",
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return labTestProcedures.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }


    public List<ProcedureRequestedDTO> getUltrasoundScanProceduresByVisit(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is "LabTest" and visit ID matches, ordered descending
        List<ProcedureRequested> UltrasoundScan = ProcedureRequested.find(
                "procedureRequestedType = ?1 and visit.id = ?2 ORDER BY id DESC", // Replace 'id' with your desired field for sorting
                "imaging",
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return UltrasoundScan.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }


    public List<ProcedureRequestedDTO> getNonLabTestNonUltrasoundProceduresByVisit(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is neither "LabTest" nor "Ultrasound" and visit ID matches, ordered descending
        List<ProcedureRequested> procedures = ProcedureRequested.find(
                "procedureRequestedType NOT IN (?1, ?2) and visit.id = ?3 ORDER BY id DESC",
                "LabTest",
                "imaging",
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return procedures.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }




    public BigDecimal getLabTestProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> labTestProcedures = ProcedureRequested.find(
                "procedureRequestedType = ?1 and visit.id = ?2 ORDER BY id DESC",
                "LabTest",
                visitId
        ).list();

        BigDecimal totalAmountSum;
        totalAmountSum = labTestProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return totalAmountSum;
    }


    public BigDecimal getScanProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "procedureRequestedType = ?1 and visit.id = ?2 ORDER BY id DESC",
                "imaging",
                visitId
        ).list();

        BigDecimal totalAmountSum;
        totalAmountSum = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return totalAmountSum;
    }



    public List<BigDecimal> getTotalCostOfProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "procedureRequestedType = ?1 and visit.id = ?2 ORDER BY id DESC",
                "Ultrasound",
                visitId
        ).list();

        List<ProcedureRequested> labTestsProcedures = ProcedureRequested.find(
                "procedureRequestedType = ?1 and visit.id = ?2 ORDER BY id DESC",
                "LabTest",
                visitId
        ).list();

        BigDecimal ultrasoundTotalAmount = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal labTotalAmount = labTestsProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return List.of(labTotalAmount, ultrasoundTotalAmount);
    }


    @Transactional
    public Response deleteProcedureRequestById(Long id) {

        ProcedureRequested procedureRequested = proceduresRequestedRepository.findById(id);

        if (procedureRequested == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        proceduresRequestedRepository.delete(procedureRequested);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }



    // final and working//











}
