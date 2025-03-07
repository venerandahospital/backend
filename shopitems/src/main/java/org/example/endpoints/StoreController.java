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

import org.example.domains.Item;
import org.example.services.StoreService;
import org.example.services.payloads.requests.StoreRequest;
import org.example.services.payloads.responses.dtos.StoreDTO;
import org.example.statics.StatusTypes;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class StoreController {

    @Inject
    StoreService storeService;

    @POST
    @Path("create-new-store")
    @Transactional
    @Operation(summary = "create-new-store", description = "create-new-store")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StoreDTO.class)))
    public Response createNewStore(StoreRequest request) {
        return Response.ok(new ResponseMessage(StatusTypes.CREATED.label, storeService.createNewStore(request))).build();
    }

    @GET
    @Path("/get-all-stores")
    @Transactional
    @Operation(summary = "get all stores", description = "get all stores.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = StoreDTO.class)))
    public Response getAllStores() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,storeService.getAllStores())).build();
    }
}
