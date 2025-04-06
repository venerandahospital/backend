package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.*;
import org.example.domains.repositories.ItemRepository;
import org.example.domains.repositories.TreatmentRequestedRepository;
import org.example.services.payloads.requests.TreatmentRequestedRequest;
import org.example.services.payloads.responses.dtos.TreatmentRequestedDTO;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class TreatmentRequestService {
    @Inject
    TreatmentRequestedRepository treatmentRequestedRepository;

    @Inject
    ShopItemService itemService;

    @Inject
    ItemRepository itemRepository;

    public static final String NOT_FOUND = "Not found!";

    public Response createNewTreatmentRequested(Long id, TreatmentRequestedRequest request) {
        // Fetch the PatientVisit and Item in one go
        PatientVisit patientVisit = PatientVisit.findById(id);
        Item item = Item.findById(request.itemId);


        // Handle not found error
        if (patientVisit == null || item == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Patient or item NOT FOUND", null))
                    .build();
        }

        // Check if stock is sufficient
        if (BigDecimal.valueOf(request.quantity).compareTo(item.stockAtHand) > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Stock at hand is insufficient. Available: " +
                            item.stockAtHand + ", Requested: " + request.quantity,
                            "INSUFFICIENT_STOCK"))
                    .build();
        }

        // Check if a TreatmentRequested with the same item and visit already exists
        TreatmentRequested existingTreatment = TreatmentRequested.find(
                "visit.id = ?1 and itemName = ?2",
                patientVisit.id,
                item.title
        ).firstResult();

        TreatmentRequestedDTO dto;

        if (existingTreatment != null) {
            // If it exists, increment the quantity and update the total amount
            existingTreatment.quantity += request.quantity;
            existingTreatment.totalAmount = BigDecimal.valueOf(existingTreatment.quantity)
                    .multiply(existingTreatment.unitSellingPrice);
            treatmentRequestedRepository.persist(existingTreatment);

            itemService.updateItemStockAtHandAfterSelling(request.quantity, item);


            dto = new TreatmentRequestedDTO(existingTreatment);
            return Response.ok(new ResponseMessage("Treatment request updated successfully", dto)).build();
        } else {
            // Otherwise, create a new TreatmentRequested record
            TreatmentRequested treatmentRequested = new TreatmentRequested();
            treatmentRequested.quantity = request.quantity;
            treatmentRequested.unitSellingPrice = item.sellingPrice;
            treatmentRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(item.sellingPrice);
            treatmentRequested.visit = patientVisit;
            treatmentRequested.itemName = item.title;

            treatmentRequestedRepository.persist(treatmentRequested);
            itemService.updateItemStockAtHandAfterSelling(request.quantity, item);

            dto = new TreatmentRequestedDTO(treatmentRequested);
            return Response.ok(new ResponseMessage("New treatment request created successfully", dto)).build();
        }
    }




    public List<TreatmentRequestedDTO> getTreatmentRequestedByVisit(Long visitId) {
        List<TreatmentRequested> treatmentGive = TreatmentRequested.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

        // Convert the results to a list of TreatmentRequestedDTO
        return treatmentGive.stream()
                .map(TreatmentRequestedDTO::new)
                .toList();
    }

    @Transactional
    public Response deleteTreatmentRequestById(Long id) {

        TreatmentRequested treatmentRequested = treatmentRequestedRepository.findById(id);

        if (treatmentRequested == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        treatmentRequestedRepository.delete(treatmentRequested);

        String itemTitle = treatmentRequested.itemName;

        Item item = itemRepository.find("title", itemTitle).firstResult();


        itemService.updateItemStockAtHandAfterDeleting(treatmentRequested.quantity, item);


        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

}