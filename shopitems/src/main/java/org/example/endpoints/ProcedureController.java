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
import org.example.services.ProcedureService;
import org.example.services.payloads.requests.PatientGroupUpdateRequest;
import org.example.services.payloads.requests.ProcedureRequest;
import org.example.services.payloads.responses.dtos.PatientGroupDTO;
import org.example.services.payloads.responses.dtos.ProcedureDTO;
import org.example.statics.StatusTypes;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class ProcedureController {

    @Inject
    ProcedureService procedureService;

    @POST
    @Path("create-new-procedure")
    @Transactional
    @Operation(summary = "new-procedure", description = "new-procedure")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class)))
    public Response createNewProcedure(ProcedureRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,procedureService.createNewProcedure(request) )).build();
    }

    @GET
    @Transactional
    @Path("/get-all-procedures")
    // @RolesAllowed({"ADMIN"})
    @Operation(summary = "Get all procedures", description = "Retrieve a list of all procedures")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class, type = SchemaType.ARRAY)))
    public Response getAllProcedures() {
        List<ProcedureDTO> procedureList = procedureService.getAllProcedures();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, procedureList)).build();
    }

    @GET
    @Path("get-all-labTest-procedures")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "get all labTest procedures", description = "get all labTest procedures")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class)))
    public Response getAllLabTestProcedures(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,procedureService.getLabTestProcedures() )).build();
    }

    @GET
    @Path("get-all-scan-procedures")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "get all scan procedures", description = "get all scan procedures")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class)))
    public Response getAllScanProcedures(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,procedureService.getScanProcedures() )).build();
    }


    @GET
    @Path("get-Other-Procedures")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "get Other Procedures", description = "get Other Procedures")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureDTO.class)))
    public Response getOtherProcedures(){
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,procedureService.getOtherProcedures() )).build();
    }



}
