package org.example.visit.endpoints;


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
import org.example.statics.StatusTypes;
import org.example.visit.services.*;
import org.example.visit.services.paloads.requests.PatientVisitRequest;
import org.example.visit.services.paloads.requests.PatientVisitStatusUpdateRequest;
import org.example.visit.services.paloads.requests.PatientVisitUpdateRequest;
import org.example.visit.services.paloads.responses.PatientVisitDTO;

import java.util.List;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class PatientVisitController {

    @Inject
    PatientVisitService patientVisitService;


    @POST
    @Path("create-new-patient-visit/{id}")
    @Transactional
    @Operation(summary = "new-patient-visit", description = "new-patient-visit")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientVisitDTO.class)))
    public Response createPatientVisit(@PathParam("id") Long id, PatientVisitRequest request){
        return Response.ok(new ResponseMessage(StatusTypes.VISIT_CREATED_SUCCESSFULLY.label,patientVisitService.createNewPatientVisit(id, request) )).build();
    }

    @GET
    @Transactional
    @Path("/get-all-patients-visits")
    @Operation(summary = "Get all patients-visits", description = "Retrieve a list of all patients visits")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientVisitDTO.class, type = SchemaType.ARRAY)))
    public Response getAllPatients() {
        List<PatientVisitDTO> patientVisitList = patientVisitService.getAllPatients();
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientVisitList)).build();
    }

    @PUT
    @Path("update-patient-visit/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update patient visit", description = "Update patient visit")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientVisitDTO.class)))
    public Response updatePatientVisit(@PathParam("id") Long id, PatientVisitUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,patientVisitService.updatePatientVisitById(id, request) )).build();
    }

    @PUT
    @Path("update-patient-visit-status/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update patient visit status", description = "Update patient visit status")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientVisitDTO.class)))
    public Response updatePatientVisitStatus(@PathParam("id") Long id, PatientVisitStatusUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,patientVisitService.updatePatientVisitStatusById(id, request) )).build();
    }

    @GET
    @Path("get-patient-Visit-List-by-id/{id}")
    @Operation(summary = "Get patientVisitList where patient id", description = "Get patientVisitList where patient id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientVisitDTO.class)))
    public Response getPatientVisitListById(@PathParam("id") Long patientId) {
        // Call the service method to get a list of InitialTriageVitalsDTO for the given visitId
        List<PatientVisitDTO> patientVisitList = patientVisitService.getVisitByPatientId(patientId);

        // Return a successful response with the list of DTOs
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label, patientVisitList)).build();
    }


    @GET
    @Path("get-latest-patient-visit-by-patient-id/{id}")
    @Operation(summary = "Get the latest patient visit by patient id", description = "Retrieve the most recent patient visit by patient id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = PatientVisitDTO.class)))
    public Response getLatestPatientVisitByPatientId(@PathParam("id") Long patientId) {

        return patientVisitService.getLatestVisitByPatientId(patientId);
    }


}
