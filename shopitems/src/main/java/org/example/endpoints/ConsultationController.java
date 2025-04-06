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
import org.example.configuration.handler.ResponseMessage;
import org.example.services.ConsultationService;
import org.example.services.payloads.requests.ConsultationRequest;
import org.example.services.payloads.requests.InitialTriageVitalsRequest;
import org.example.services.payloads.responses.dtos.ConsultationDTO;
import org.example.services.payloads.responses.dtos.InitialTriageVitalsDTO;
import org.example.statics.StatusTypes;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")


public class ConsultationController {

    @Inject
    ConsultationService consultationService;

    @POST
    @Path("create-new-Consultation/{id}")
    @Transactional
    @Operation(summary = "new-Consultation", description = "new-Consultation")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ConsultationDTO.class)))
    public Response createConsultation(@PathParam("id") Long id, ConsultationRequest request){
        return consultationService.createNewConsultation(id, request);
    }

}
