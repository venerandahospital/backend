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
import org.example.services.ProcedureRequestedService;
import org.example.services.payloads.requests.InitialVitalUpdateRequest;
import org.example.services.payloads.requests.ProcedureRequestedRequest;
import org.example.services.payloads.requests.ProcedureRequestedUpdateRequest;
import org.example.services.payloads.responses.dtos.InitialTriageVitalsDTO;
import org.example.services.payloads.responses.dtos.ProcedureRequestedDTO;
import org.example.statics.StatusTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")


public class ProcedureRequestedController {

    @Inject
    ProcedureRequestedService procedureRequestedService;


    @POST
    @Path("create-new-procedure-requested/{id}")
    @Transactional
    @Operation(summary = "new-procedureRequested", description = "new-procedureRequested")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response createNewProcedureRequested(@PathParam("id") Long id, ProcedureRequestedRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.REQUESTED.label,procedureRequestedService.createNewProcedureRequested(id, request) )).build();
    }

    @GET
    @Path("get-procedure-Requested-with-type-LabTest-by-visit-id/{id}")
    @Operation(summary = "Get procedureRequested where type is LabTest and visit id", description = "Get procedureRequested where type is LabTest and visit id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getProcedureRequestedLabTestsByVisitId(@PathParam("id") Long visitId) {
        List<ProcedureRequestedDTO> labTestProcedureRequested = procedureRequestedService.getLabTestProceduresByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, labTestProcedureRequested)).build();
    }

    @GET
    @Path("get-ultrasound-scan-by-visit-id/{id}")
    @Operation(summary = "get-ultrasound-scan-by-visit-id", description = "get-ultrasound-scan-by-visit-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getUltrasoundScanByVisitId(@PathParam("id") Long visitId) {
        List<ProcedureRequestedDTO> ultrasoundScanProcedureRequested = procedureRequestedService.getUltrasoundScanProceduresByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, ultrasoundScanProcedureRequested)).build();
    }

    @PUT
    @Path("update-procedure-requested/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update procedure requested", description = "Update procedure requested")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response updateProcedureRequested(@PathParam("id") Long id, ProcedureRequestedUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,procedureRequestedService.updateProcedureRequestedById(id, request) )).build();
    }

    @GET
    @Path("get-total-cost-of-all-lab-tests/{id}")
    @Operation(summary = "get-total-cost-of-all-lab-tests", description = "get-total-cost-of-all-lab-tests")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema()))
    public Response getProcedureRequestedLabTestsBySumVisitId(@PathParam("id") Long visitId) {
        BigDecimal totalCostLabTests;
        totalCostLabTests = procedureRequestedService.getLabTestProceduresAndSumByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, totalCostLabTests)).build();
    }

    @GET
    @Path("get-total-cost-of-all-Scans-by-visit-id/{id}")
    @Operation(summary = "get-total-cost-of-all-Scans", description = "get-total-cost-of-all-Scans")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema()))
    public Response getProcedureRequestedScanBySumVisitId(@PathParam("id") Long visitId) {
        BigDecimal totalCostScans;
        totalCostScans = procedureRequestedService.getScanProceduresAndSumByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, totalCostScans)).build();
    }





    @GET
    @Path("get-other-procedures-by-visit-id/{id}")
    @Operation(summary = "get-other-procedures-by-visit-id", description = "get-other-procedures-by-visit-id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ProcedureRequestedDTO.class)))
    public Response getNonLabTestNonUltrasoundProceduresByVisit(@PathParam("id") Long visitId) {
        List<ProcedureRequestedDTO> otherProceduresRequested = procedureRequestedService.getNonLabTestNonUltrasoundProceduresByVisit(visitId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, otherProceduresRequested)).build();
    }


    @DELETE
    @Path("/delete-requested-procedure-by-id/{id}")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "delete requested procedure by id", description = "delete requested procedure by id")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteRequestedProcedureById(@PathParam("id") Long id){
        return procedureRequestedService.deleteProcedureRequestById(id);
    }







}
