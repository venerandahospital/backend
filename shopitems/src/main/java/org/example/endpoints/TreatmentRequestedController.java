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
import org.example.services.TreatmentRequestService;
import org.example.services.payloads.requests.ProcedureRequestedRequest;
import org.example.services.payloads.requests.TreatmentRequestedRequest;
import org.example.services.payloads.responses.dtos.ProcedureRequestedDTO;
import org.example.services.payloads.responses.dtos.TreatmentRequestedDTO;
import org.example.statics.StatusTypes;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class TreatmentRequestedController {

    @Inject
    TreatmentRequestService treatmentRequestService;

    @POST
    @Path("create-new-treatmentRequest")
    @Transactional
    @Operation(summary = "new-treatmentRequest", description = "new-treatmentRequest")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentRequestedDTO.class)))
    public Response createNewTreatmentRequested(TreatmentRequestedRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,treatmentRequestService.createNewTreatmentRequested(request) )).build();
    }

    @GET
    @Path("get-treatment-requested-by-visit-id/{id}")
    @Operation(summary = "get-treatment-requested-by-visit-id", description = "get-treatment-requested-by-visit-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = TreatmentRequestedDTO.class)))
    public Response getTreatmentByVisitId(@PathParam("id") Long visitId) {
        List<TreatmentRequestedDTO> treatmentRequested = treatmentRequestService.getTreatmentRequestedByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, treatmentRequested)).build();
    }
}
