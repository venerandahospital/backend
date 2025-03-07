package org.example.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.services.PatientVisitService;
import org.example.services.PaymentService;
import org.example.services.payloads.requests.PatientVisitRequest;
import org.example.services.payloads.requests.PaymentRequest;
import org.example.services.payloads.responses.dtos.PatientVisitDTO;
import org.example.services.payloads.responses.dtos.PaymentDTO;
import org.example.statics.StatusTypes;

import java.util.List;


@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class PaymentController {

    @Inject
    PaymentService paymentsService;


    @POST
    @Path("create-new-payment")
    @Transactional
    @Operation(summary = "new-payment", description = "new-payment")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PaymentDTO.class)))
    public Response createNewPayment(PaymentRequest request) {
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label, paymentsService.createNewPayment(request))).build();
    }

    @GET
    @Path("get-payment-List-by-visit-id/{id}")
    @Operation(summary = "Get payment List where visit id", description = "Get payment List where visit id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientVisitDTO.class)))
    public Response getPaymentListByVisitId(@PathParam("id") Long visitId) {
        // Call the service method to get a list of InitialTriageVitalsDTO for the given visitId
        List<PaymentDTO> paymentList = paymentsService.getPaymentsByVisitId(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, paymentList)).build();
    }


}
