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
import org.example.services.InitialTriageVitalsService;
import org.example.services.PatientVisitService;
import org.example.services.payloads.requests.InitialTriageVitalsRequest;
import org.example.services.payloads.requests.PatientVisitRequest;
import org.example.services.payloads.responses.dtos.InitialTriageVitalsDTO;
import org.example.services.payloads.responses.dtos.PatientDTO;
import org.example.services.payloads.responses.dtos.PatientVisitDTO;
import org.example.statics.StatusTypes;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class InitialTriageVitalsController {

    @Inject
    InitialTriageVitalsService initialTriageVitalsService;

    @POST
    @Path("create-new-InitialTriageVitals")
    @Transactional
    @Operation(summary = "new-InitialTriageVitals", description = "new-InitialTriageVitals")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InitialTriageVitalsDTO.class)))
    public Response createInitialTriageVitalsVisit(InitialTriageVitalsRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,initialTriageVitalsService.createNewInitialTriageVitals(request) )).build();
    }

    @GET
    @Transactional
    @Path("/get-all-InitialTriageVitals")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get allInitialTriageVitals", description = "Retrieve a list of all InitialTriageVitals")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InitialTriageVitalsDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatients() {
        List<InitialTriageVitalsDTO> initialTriageVitalsList = initialTriageVitalsService.getAllInitialTriageVitals();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, initialTriageVitalsList)).build();
    }

    @GET
    @Path("get-InitialTriageVitals-visit/{id}")
    //@RolesAllowed({"ADMIN"})
    @Operation(summary = "Get patInitialTriageVitals where visit id", description = "Get patInitialTriageVitals where visit id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = InitialTriageVitalsDTO.class)))
    public Response getInitialTriageVitalsById(@PathParam("id") Long visitId) {
        InitialTriageVitalsDTO InitialTriageVital = initialTriageVitalsService.getInitialTriageVitalWhereVisitId(visitId);
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, InitialTriageVital)).build();
    }
}
