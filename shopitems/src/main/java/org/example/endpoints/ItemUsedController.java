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
import org.example.domains.ShoppingCart;
import org.example.services.ItemUsedService;
import org.example.services.payloads.requests.ItemUsedRequest;
import org.example.services.payloads.responses.basicResponses.ItemUsedResponse;
import org.example.services.payloads.responses.basicResponses.ShoppingCartResponse;
import org.example.services.payloads.responses.dtos.ItemUsedDTO;

@Path("Patient-management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Patient Management Module", description = "Patient Management")

public class ItemUsedController {

    @Inject
    ItemUsedService itemUsedService;

    @POST
    @Path("/add-to-item-used")
    //@RolesAllowed({"ADMIN"})
    @Transactional
    @Operation(summary = "add a new item used to make lab test", description = "add a new item used to make lab test.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemUsedDTO.class)))
    public Response addItemUsedToLabTest(ItemUsedRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label,itemUsedService.addToItemUsedToLabTest(request))).build();
    }

    @GET
    @Path("/get-used-items/{id}")
    @Transactional
    @Operation(summary = "get all lab used items", description = "get all lab used items.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemUsedResponse.class)))
    public Response getUsedItems(@PathParam("id") Long labTestId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,itemUsedService.getCart(labTestId))).build();
    }

    @GET
    @Path("/get-All-used-items/{id}")
    @Transactional
    @Operation(summary = "get all used items", description = "get all used items.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ItemUsedResponse.class)))
    public Response getAllUsedItems(@PathParam("id") Long labTestId) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,itemUsedService.getAllCart(labTestId))).build();
    }


}
