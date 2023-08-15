package org.example.endpoints;

import jakarta.annotation.security.RolesAllowed;
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
import org.example.domains.User;
import org.example.services.ShopItemService;
import org.example.services.payloads.ShopItemRequest;
import org.example.services.payloads.ShopItemResponse;
import org.example.configuration.handler.*;
import org.example.services.payloads.ShopItemUpdateRequest;

@Path("/shop-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Shop Management Module", description = "Shop Management")
public class ShopItemController {

    @Inject
    ShopItemService shopItemService;

    @POST
    @Path("/add-item")
    @Transactional
    @Operation(summary = "add a new shopItem", description = "add a new shopItem.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)))
    public Response addShopItem(ShopItemRequest request) {
        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label,shopItemService.addShopItem(request))).build();
    }

    @GET
    @Path("/get-all-Items")
    @Transactional
    @Operation(summary = "get all shopItems", description = "get all shopItems.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)))
    public Response getShopItems() {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shopItemService.getAllShopItems())).build();
    }

    @GET
    @Path("{id}")
    @Transactional
    @Operation(summary = "get shopItem by id", description = "get shopItem by id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)))
    public Response getShopItemById(@PathParam("id") Long id) {
        return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,shopItemService.getShopItemById(id))).build();
    }

    @DELETE
    @Path("delete-all")
    @Transactional
    @Operation(summary = "delete all shopItems", description = "delete all shopItems.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteAllItems(){
        shopItemService.deleteAllShopItems();
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();

    }

    @PUT
    @Path("{id}")
    @Transactional
    @Operation(summary = "Update shopItem by Id", description = "Update shopItem by Id")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
    public Response updateShopItem(@PathParam("id") Long id, ShopItemUpdateRequest request){
        return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,shopItemService.updateShopItemById(id, request) )).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    @Operation(summary = "delete shopItem by id ", description = "delete shopItem by id.")
    @APIResponse(description = "Successful", responseCode = "200")
    public Response deleteItemById(@PathParam("id") Long id){
        shopItemService.deleteShopItemById(id);
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();

    }


}
