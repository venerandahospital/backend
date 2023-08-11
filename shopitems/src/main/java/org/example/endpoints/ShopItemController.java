package org.example.endpoints;

import jakarta.annotation.security.RolesAllowed;
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
import org.example.services.ShopItemService;
import org.example.services.payloads.ShopItemRequest;
import org.example.services.payloads.ShopItemResponse;
import org.example.configuration.handler.*;

@Path("/shop-item")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Shop - Item", description = "Shop Item Management")
public class ShopItemController {

    @Inject
    ShopItemService shopItemService;

    @POST
    @Transactional
    @Operation(summary = "create a new ShopItem", description = "This will create a new ShopItem.")
    @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = ShopItemResponse.class)))
    public Response addShopItem(ShopItemRequest request) {

        return Response.ok(new ResponseMessage(ActionMessages.SAVED.label,shopItemService.addShopItem(request))).build();
    }

}
