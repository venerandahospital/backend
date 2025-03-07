package org.example.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.PatientVisit;
import org.example.services.InvoiceService;
import org.example.services.PaymentService;
import org.example.services.payloads.requests.*;
import org.example.services.payloads.responses.dtos.InvoiceDTO;
import org.example.services.payloads.responses.dtos.PatientDTO;
import org.example.services.payloads.responses.dtos.PaymentDTO;
import org.example.statics.StatusTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class InvoiceController {


    @Inject
    InvoiceService invoiceService;


    @POST
    @Path("create-new-Invoice/{id}")
    @Transactional
    @Operation(summary = "new-invoice", description = "new-invoice")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InvoiceDTO.class)))
    public Response createNewInvoice(@PathParam("id") Long visitId) {
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label, invoiceService.createInvoice(visitId))).build();
    }

    @GET
    @Path("get-total-cost-of-every-service-by-visit-id/{id}")
    @Operation(summary = "Get total cost of every service by visit ID", description = "Retrieve the total costs of every service for a given visit ID.")
    @APIResponse(
            description = "Successful",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = ResponseMessage.class))
    )
    public Response getTotalCostOfEveryServiceVisitId(@PathParam("id") Long visitId) {
        // Call the updated service method
        Map<String, BigDecimal> totalCostOfEveryService = invoiceService.getInvoiceSubTotal(visitId);

        // Return a successful response with the Map
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, totalCostOfEveryService)).build();
    }

    @GET
    @Transactional
    @Path("/get-all-invoices")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all invoices", description = "Retrieve a list of all invoices")
    @APIResponse(
            description = "Successful",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = InvoiceDTO.class, type = SchemaType.ARRAY))
    )
    public Response getAllInvoices() {
        List<InvoiceDTO> invoiceList = invoiceService.getAllInvoices();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, invoiceList)).build();
    }

    @PUT
    @Path("update-invoice/{id}")
//@RolesAllowed({"ADMIN", "CUSTOMER"})
    @Transactional
    @Operation(summary = "Update invoice", description = "Update an existing invoice by ID.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InvoiceDTO.class)))
    @APIResponse(description = "Invoice not found", responseCode = "404")
    public Response updateInvoice(@PathParam("id") Long id, InvoiceUpdateRequest request) {

            InvoiceDTO updatedInvoice = invoiceService.updateInvoice(id, request);
            return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label, updatedInvoice)).build();

    }


    @GET
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Path("invoice/generate-pdf/{id}")
    @Operation(summary = "invoice pdf", description = "invoice pdf download")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response generateAndReturnInvoicePdf(@PathParam("id") Long visitId) {
        return invoiceService.generateAndReturnInvoicePdf(visitId);
    }

}
