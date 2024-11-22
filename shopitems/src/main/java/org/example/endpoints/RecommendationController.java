package org.example.endpoints;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.repositories.RecommendationRepository;
import org.example.services.RecommendationService;
import org.example.services.payloads.requests.ProcedureRequest;
import org.example.services.payloads.requests.RecommendationRequest;
import org.example.services.payloads.responses.dtos.ProcedureDTO;
import org.example.services.payloads.responses.dtos.RecommendationDTO;
import org.example.statics.StatusTypes;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class RecommendationController {

    @Inject
    RecommendationService recommendationService;

    @POST
    @Path("create-new-Recommendation")
    @Transactional
    @Operation(summary = "new-Recommendation", description = "new-Recommendation")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RecommendationDTO.class)))
    public Response createNewRecommendation(RecommendationRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,recommendationService.createNewRecommendation(request) )).build();
    }
}
