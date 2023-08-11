package org.example.endpoints;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.User;
import org.example.services.UserService;
import org.example.services.payloads.*;
import org.example.statics.StatusTypes;

@Path("management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Management - User Management", description = "User Management")

public class UserController {

        @Inject
        UserService userService;

        @POST
        @Path("create")
        @Transactional
        @Operation(summary = "Create User", description = "Create user")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response createUser(UserRequest request, @Context SecurityContext ctx){
            return Response.ok(new ResponseMessage(StatusTypes.CREATED.label,userService.createNewUser(request) )).build();
        }

        @GET
        @Path("{id}")
        @Operation(summary = "Get User by Id", description = "Get User by Id")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response getById(@PathParam("id") Long id){
            return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,userService.getById(id) )).build();
        }

        @PUT
        @Path("{id}")
        @Transactional
        @Operation(summary = "Update User by Id", description = "Update User by Id")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response update(@PathParam("id") Long id, UpdateRequest request){
            return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,userService.update(id, request) )).build();
        }

        @GET
        @Operation(summary = "get all Users", description = "get all Users")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class,type = SchemaType.ARRAY)))
        public Response getAllAgents(){
            return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,userService.getAllAgents() )).build();
        }

        @PUT
        @Path("update-password/{id}")
        @Transactional
        @Operation(summary = "Update User Password by Id", description = "Update User Password by Id")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response updatePassword(@PathParam("id") Long id, UpdatePasswordRequest request){
            return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,userService.updatePassword(id, request) )).build();
        }

        @PUT
        @Path("update-User-role/{id}")
        @Transactional
        @Operation(summary = "Update User Role by Id", description = "Update User Role by Id")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
        public Response updateRole(@PathParam("id") Long id, RoleRequest request){
            return Response.ok(new ResponseMessage(ActionMessages.UPDATED.label,userService.updateRole(id, request) )).build();
        }

        @GET
        @Path("/roles")
        @Operation(summary = "get roles", description = "get roles")
        @APIResponse(description = "Successful", responseCode = "200", content = @Content(schema = @Schema(implementation = RoleResponse.class)))
        public Response role(){
            return Response.ok(new ResponseMessage(ActionMessages.FETCHED.label,userService.roles() )).build();
        }
    }


