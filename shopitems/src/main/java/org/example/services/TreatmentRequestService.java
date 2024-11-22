package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.domains.*;
import org.example.domains.repositories.TreatmentRequestedRepository;
import org.example.services.payloads.requests.ProcedureRequestedRequest;
import org.example.services.payloads.requests.TreatmentRequestedRequest;
import org.example.services.payloads.responses.dtos.ProcedureRequestedDTO;
import org.example.services.payloads.responses.dtos.TreatmentRequestedDTO;

import java.math.BigDecimal;

@ApplicationScoped
public class TreatmentRequestService {
    @Inject
    TreatmentRequestedRepository treatmentRequestedRepository;

    public static final String NOT_FOUND = "Not found!";

    public TreatmentRequestedDTO createNewTreatmentRequested(TreatmentRequestedRequest request) {
        // Fetch the PatientVisit and Item in one go
        PatientVisit patientVisit = PatientVisit.findById(request.visitID);
        Item item = Item.findById(request.itemId);

        // Throw exception if either of them is not found
        if (patientVisit == null || item == null) {
            throw new IllegalArgumentException(NOT_FOUND); // Handle not found error
        }

        // Check if a TreatmentRequested with the same item and visit already exists
        TreatmentRequested existingTreatment = TreatmentRequested.find(
                "visit.id = ?1 and item.id = ?2",
                patientVisit.id,
                item.id
        ).firstResult();

        if (existingTreatment != null) {
            // If it exists, increment the quantity and update the total amount
            existingTreatment.quantity += 1;
            existingTreatment.totalAmount = BigDecimal.valueOf(existingTreatment.quantity)
                    .multiply(existingTreatment.unitSellingPrice);
            treatmentRequestedRepository.persist(existingTreatment); // Persist the updated entity
            return new TreatmentRequestedDTO(existingTreatment);
        } else {
            // Otherwise, create a new TreatmentRequested record
            TreatmentRequested treatmentRequested = new TreatmentRequested();
            treatmentRequested.quantity = request.quantity;
            treatmentRequested.unitSellingPrice = item.sellingPrice;
            treatmentRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(item.sellingPrice);
            treatmentRequested.visit = patientVisit;
            treatmentRequested.item = item;
            treatmentRequestedRepository.persist(treatmentRequested);
            return new TreatmentRequestedDTO(treatmentRequested);
        }
    }

}
