package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.domains.Procedure;
import org.example.domains.ProcedureRequested;
import org.example.domains.PatientVisit;
import org.example.domains.repositories.ProcedureRequestedRepository;
import org.example.services.payloads.requests.ProcedureRequestedRequest;
import org.example.services.payloads.responses.dtos.ProcedureRequestedDTO;

import java.math.BigDecimal;

@ApplicationScoped
public class ProcedureRequestedService {
    @Inject
    ProcedureRequestedRepository proceduresRequestedRepository;

    public static final String NOT_FOUND = "Not found!";

    public ProcedureRequestedDTO createNewProcedureRequested(ProcedureRequestedRequest request) {
        // Fetch the PatientVisit and Procedure in one go
        PatientVisit patientVisit = PatientVisit.findById(request.visitID);
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
            procedureRequested.Report = request.Report;
            procedureRequested.orderedBy = request.orderedBy;
            procedureRequested.doneBy = request.doneBy;
            procedureRequested.unitSellingPrice = procedure.unitSellingPrice;
            procedureRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(procedure.unitSellingPrice);

            procedureRequested.visit = patientVisit;
            procedureRequested.procedure = procedure;

            proceduresRequestedRepository.persist(procedureRequested);
            return new ProcedureRequestedDTO(procedureRequested);
        }
    }

}
