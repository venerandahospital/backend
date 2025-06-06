package org.example.cafeteria.finance.invoice.endpoints;

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
import org.example.finance.invoice.services.payloads.responses.InvoiceDTO;
import org.example.finance.invoice.services.InvoiceService;
import org.example.finance.invoice.services.payloads.requests.InvoiceUpdateRequest;
import org.example.statics.StatusTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Path("cafeteria-invoice-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "cafeteria invoice Management Module", description = "cafeteria invoice Management")

public class CanteenInvoiceController {


    @Inject
    InvoiceService invoiceService;


    @POST
    @Path("create-new-canteen-Invoice/{id}")
    @Transactional
    @Operation(summary = "new-invoice", description = "new-invoice")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InvoiceDTO.class)))
    public Response createNewInvoice(@PathParam("id") Long visitId) {
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label, invoiceService.createInvoice(visitId))).build();
    }

    @GET
    @Path("get-total-cost-of-every-canteen-service-by-sale-id/{id}")
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
    @Path("get-total-cost-of-every-service-by-buyer-id/{id}")
    @Operation(summary = "Get total cost of every service by patient ID", description = "Retrieve the total costs of every service for a given visit ID.")
    @APIResponse(
            description = "Successful",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = ResponseMessage.class))
    )
    public Response getTotalCostOfEveryServicePatientId(@PathParam("id") Long patientId) {
        // Call the updated service method
        Map<String, BigDecimal> totalCostOfEveryServiceByPatientId = invoiceService.getTotalPatientBalanceDue(patientId);

        // Return a successful response with the Map
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, totalCostOfEveryServiceByPatientId)).build();
    }

    @GET
    @Transactional
    @Path("/get-all-sales")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all sales", description = "Retrieve a list of all sales")
    @APIResponse(
            description = "Successful",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = InvoiceDTO.class, type = SchemaType.ARRAY))
    )
    public Response getAllInvoices() {
        List<InvoiceDTO> invoiceList = invoiceService.getAllInvoices();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, invoiceList)).build();
    }



    @GET
    @Transactional
    @Path("/get-sale-by-visit-id/{visitId}")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get invoice by visit ID", description = "Retrieve a single invoice based on visit ID")
    @APIResponse(
            description = "Successful",
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = InvoiceDTO.class))
    )
    public Response getInvoiceByVisitId(@PathParam("visitId") Long visitId) {

        return invoiceService.getInvoiceByVisitId(visitId);
    }


    @PUT
    @Path("update-sale/{id}")
    //@RolesAllowed({"ADMIN", "CUSTOMER"})
    @Transactional
    @Operation(summary = "Update invoice", description = "Update an existing invoice by ID.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InvoiceDTO.class)))
    @APIResponse(description = "Invoice not found", responseCode = "404")
    public Response updateInvoice(@PathParam("id") Long id, InvoiceUpdateRequest request) {
            return invoiceService.updateInvoice(id, request);

    }


    @GET
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Path("sale/generate-pdf/{id}")
    @Operation(summary = "invoice pdf", description = "invoice pdf download")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = Response.class)))
    public Response generateAndReturnInvoicePdf(@PathParam("id") Long visitId) {
        return invoiceService.generateAndReturnInvoicePdf(visitId);
    }

    @DELETE
    @Path("/delete-sale-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete invoice by id", description = "delete invoice by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteInvoiceById(@PathParam("id") Long id){
        return invoiceService.deleteInvoice(id);
    }

}
