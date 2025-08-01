package org.example.diagnostics.ultrasoundScan.generalUs.endpoints;

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
import org.example.client.services.payloads.requests.PatientUpdateRequest;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.diagnostics.ultrasoundScan.generalUs.services.GeneralUsService;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsRequest;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsUpdateRequest;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.responses.GeneralUsDTO;


@Path("diagnostics-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Ultrasound Scan Management Module", description = "Ultrasound Scan Management")

public class GeneralUsController {


    @Inject
    GeneralUsService generalUsService;

    @POST
    @Path("create-new-general-scan-report")
    @Transactional
    @Operation(summary = "create new general scan report", description = "create new general scan report")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = GeneralUsDTO.class)))
    public Response createGeneralScanReport(GeneralUsRequest request) {
        return generalUsService.createGeneralUsReport(request);

    }

    @PUT
    @Path("update-scan-report/{id}")
    //@RolesAllowed({"ADMIN","CUSTOMER"})
    @Transactional
    @Operation(summary = "Update scan report", description = "Update scan report")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = GeneralUsDTO.class)))
    public Response updateScanReport(@PathParam("id") Long id, GeneralUsUpdateRequest request){
        return generalUsService.updateScanReportById(id, request);
    }
}
