package org.example.fileresource.endpoint;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.example.domains.FileResource;
import org.example.fileresource.service.FileService;
import org.example.fileresource.service.payload.FileRequest;

@Path("file")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "File Resources", description = "File resource management")
public class FileController {


    @Inject
    FileService fileService;

    @GET
    @Path("id/{id}")
    public byte[] getSingleId(@PathParam("id") Long id) {

        return fileService.getFileById(id);

    }

    @GET
    @Path("url/{url}")
    public byte[] getSingleUrl(@PathParam("url") String url) {

        return fileService.getFileByUrl(url);

    }

    @POST
    @Transactional
    public Response create(FileRequest file) {

        FileResource entity = fileService.create(file);

        return Response.ok(entity).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, FileRequest file) {

        FileResource entity = fileService.update(id, file);
        entity.persist();
        return Response.ok(entity).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {

        if (Boolean.TRUE.equals(fileService.delete(id))) {
            return Response.ok().build();
        }
        throw new WebApplicationException("There was an error deleting this record!", 500);
    }
}
