package org.example.treatment.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.services.payloads.requests.InvoiceUpdateRequest;
import org.example.item.domain.repositories.ItemRepository;
import org.example.finance.invoice.services.InvoiceService;
import org.example.item.domain.Item;
import org.example.item.services.ShopItemService;
import org.example.treatment.domains.TreatmentRequested;
import org.example.treatment.domains.repositories.TreatmentRequestedRepository;
import org.example.treatment.services.payloads.responses.TreatmentRequestedDTO;
import org.example.treatment.services.payloads.requests.TreatmentRequestedRequest;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class TreatmentRequestService {
    @Inject
    TreatmentRequestedRepository treatmentRequestedRepository;

    @Inject
    ShopItemService itemService;

    @Inject
    InvoiceService invoiceService;

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

        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        BigDecimal totalBalanceDue = invoiceService.calculateTotalBalanceDueForClosedVisits(patientVisit.patient.id);

        if (totalBalanceDue.compareTo(BigDecimal.ZERO) > 0 &&
                (patientVisit.patient.patientGroup == null || !patientVisit.patient.patientGroup.groupName.equalsIgnoreCase("veneranda medical"))) {

            // There is an unpaid balance and the patient is not part of the "family" group
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot access any service. Patient has a debt of: "
                            + totalBalanceDue + " and doesn't belong to veneranda medical group"+". Please clear the debt first. or Contact Admin"))
                    .build();
        }



        // Check if stock is sufficient
        if (request.quantity.compareTo(item.stockAtHand) > 0) {
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
            existingTreatment.quantity = existingTreatment.quantity.add(request.quantity);
            existingTreatment.totalAmount = existingTreatment.quantity.multiply(existingTreatment.unitSellingPrice);
            treatmentRequestedRepository.persist(existingTreatment);

            itemService.updateItemStockAtHandAfterSelling(request.quantity, item);

            dto = new TreatmentRequestedDTO(existingTreatment);
            return Response.ok(new ResponseMessage("Treatment request updated successfully", dto)).build();
        } else {
            // Otherwise, create a new TreatmentRequested record
            TreatmentRequested treatmentRequested = new TreatmentRequested();
            treatmentRequested.quantity = request.quantity;
            treatmentRequested.shelfNumber = item.shelfNumber;
            treatmentRequested.provisionalQuantity = request.quantity;
            treatmentRequested.status = "pending";
            treatmentRequested.unitSellingPrice = item.sellingPrice;
            treatmentRequested.totalAmount = request.quantity.multiply(item.sellingPrice);
            treatmentRequested.provisionalTotalAmount = request.quantity.multiply(item.sellingPrice);
            treatmentRequested.visit = patientVisit;
            treatmentRequested.amountPerFrequency = request.amountPerFrequency;
            treatmentRequested.durationValue = request.durationValue;
            treatmentRequested.durationUnit = request.durationUnit;
            treatmentRequested.instructions = request.instructions;
            treatmentRequested.route = request.route;
            treatmentRequested.frequencyValue = request.frequencyValue;
            treatmentRequested.frequencyUnit = request.frequencyUnit;
            treatmentRequested.itemName = item.title;
            treatmentRequested.itemId = item.id;

            treatmentRequestedRepository.persist(treatmentRequested);
            itemService.updateItemStockAtHandAfterSelling(request.quantity, item);

            dto = new TreatmentRequestedDTO(treatmentRequested);
            return Response.ok(new ResponseMessage("New treatment request created successfully", dto)).build();
        }
    }


    public Response updateTreatmentRequested(Long treatmentId, TreatmentRequestedRequest request) {
        // Fetch the existing treatment request
        TreatmentRequested treatment = TreatmentRequested.findById(treatmentId);
        if (treatment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Treatment request not found", null))
                    .build();
        }

        PatientVisit visit = treatment.visit;
        if (visit == null || "closed".equals(visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot update treatment. Visit is closed.", null))
                    .build();
        }

        // Fetch new item if provided
        Item item = Item.findById(request.itemId);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Item not found", null))
                    .build();
        }

        // Check if quantity has changed and stock is sufficient
        if (request.quantity != null && request.quantity.compareTo(treatment.quantity) > 0) {
            BigDecimal additionalNeeded = request.quantity.subtract(treatment.quantity);
            if (additionalNeeded.compareTo(item.stockAtHand) > 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("Insufficient stock. Additional required: " + additionalNeeded + ", Available: " + item.stockAtHand,
                                "INSUFFICIENT_STOCK"))
                        .build();
            }

            // Reduce stock by additional quantity
            itemService.updateItemStockAtHandAfterSelling(additionalNeeded, item);
        }

        // Update fields
        treatment.quantity = request.quantity;
        treatment.provisionalQuantity = request.quantity;
        treatment.unitSellingPrice = item.sellingPrice;
        assert request.quantity != null;
        treatment.totalAmount = request.quantity.multiply(item.sellingPrice);
        treatment.provisionalTotalAmount = treatment.totalAmount;
        treatment.durationValue = request.durationValue;
        treatment.amountPerFrequency = request.amountPerFrequency;
        treatment.durationUnit = request.durationUnit;
        treatment.frequencyValue = request.frequencyValue;
        treatment.frequencyUnit = request.frequencyUnit;
        treatment.route = request.route;
        treatment.instructions = request.instructions;

        treatment.itemName = item.title;
        treatment.itemId = item.id;

        treatmentRequestedRepository.persist(treatment);

        TreatmentRequestedDTO dto = new TreatmentRequestedDTO(treatment);
        return Response.ok(new ResponseMessage("Treatment request updated successfully", dto)).build();
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

        Invoice invoice = Invoice.find("visit.id", treatmentRequested.visit.id).firstResult();

        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.tax = BigDecimal.ZERO;
        request.discount = BigDecimal.ZERO;

        invoiceService.updateInvoice(invoice.id, request);


        treatmentRequestedRepository.delete(treatmentRequested);

        String itemTitle = treatmentRequested.itemName;

        Item item = itemRepository.find("title", itemTitle).firstResult();


        itemService.updateItemStockAtHandAfterDeleting(treatmentRequested.quantity, item);




        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }

}