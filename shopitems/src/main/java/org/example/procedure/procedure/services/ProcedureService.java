package org.example.procedure.procedure.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ResponseMessage;
import org.example.item.domain.repositories.ItemRepository;
import org.example.procedure.procedure.domains.Procedure;
import org.example.procedure.procedure.domains.ProcedureCategory;
import org.example.procedure.procedure.domains.ProcedureType;
import org.example.procedure.procedure.domains.repositories.ProcedureCategoryRepository;
import org.example.procedure.procedure.domains.repositories.ProcedureRepository;
import org.example.procedure.procedure.domains.repositories.ProcedureTypeRepository;
import org.example.procedure.procedure.services.payloads.requests.*;
import org.example.procedure.procedure.services.payloads.responses.ProcedureCategoryDTO;
import org.example.procedure.procedure.services.payloads.responses.ProcedureDTO;
import org.example.procedure.procedure.services.payloads.responses.ProcedureTypeDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcedureService {

    @Inject
    ProcedureRepository procedureRepository;

    @Inject
    ProcedureTypeRepository procedureTypeRepository;

    @Inject
    ProcedureCategoryRepository procedureCategoryRepository;

    @Inject
    ItemRepository itemRepository;

    public static final String NOT_FOUND = "Lab test not found!";
    public static final String INVALID_REQUEST = "Invalid request data!";

    /**
     * Creates a new LabTest based on the provided request.
     *
     * @param request LabTestRequest containing lab test details
     * @return LabTestDTO representing the created lab test
     */
    @Transactional
    public Response createNewProcedure(ProcedureRequest request) {

        if (request.category == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("A Service with the same first and second names already exists", null))
                    .build();
        }

        boolean exists = Procedure.find(
                "procedureName = ?1 and category = ?2",
                request.procedureName,
                request.category
        ).firstResultOptional().isPresent();


        if (exists) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Procedure with name '" + request.procedureType
                            + "' and category '" + request.category + "' already exists.", null))
                    .build();
        }

        // Map request to entity
        Procedure procedure = new Procedure();
        procedure.procedureType = request.procedureType;
        procedure.procedureName = request.procedureName;
        procedure.category = request.category;
        procedure.description = request.description;
        procedure.unitSellingPrice = request.unitSellingPrice;
        procedure.unitCostPrice = request.unitCostPrice;

        // Persist the new procedure
        procedureRepository.persist(procedure);

        //return new ProcedureDTO(procedure);
        return Response.ok(new ResponseMessage("New Service created successfully", new ProcedureDTO(procedure))).build();

    }

    @Transactional
    public Response createNewProcedureCategory(ProcedureCategoryRequest request) {

        // Check if a procedure with the same name and type already exists
        boolean exists = ProcedureCategory.find(
                "procedureCategory = ?1",
                request.procedureCategory
        ).firstResultOptional().isPresent();

        if (exists) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Procedure category with name '" + request.procedureCategory
                            + "' and category already exists.", null))
                    .build();
        }

        // Map request to entity
        ProcedureCategory category = new ProcedureCategory();
        category.procedureCategory = request.procedureCategory;
        category.categoryDescription = request.categoryDescription;


        // Persist the new procedure
        procedureCategoryRepository.persist(category);

        //return new ProcedureDTO(procedure);
        return Response.ok(new ResponseMessage("New procedure category created successfully", new ProcedureCategoryDTO(category))).build();

    }


    @Transactional
    public Response createNewProcedureType(ProcedureTypeRequest request) {

        // Check if a procedure with the same name and type already exists
        boolean exists = ProcedureType.find(
                "procedureType = ?1",
                request.procedureType
        ).firstResultOptional().isPresent();

        if (exists) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Procedure Type with name '" + request.procedureType
                            + "' and Type already exists.", null))
                    .build();
        }

        // Map request to entity
        ProcedureType type = new ProcedureType();
        type.procedureType = request.procedureType;
        type.typeDescription = request.typeDescription;


        // Persist the new procedure
        procedureTypeRepository.persist(type);

        //return new ProcedureDTO(procedure);
        return Response.ok(new ResponseMessage("New procedure category created successfully", new ProcedureTypeDTO(type))).build();

    }

    @Transactional
    public List<ProcedureCategoryDTO> getAllProceduresCategories() {
        return procedureCategoryRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ProcedureCategoryDTO::new)
                .toList();
    }

    @Transactional
    public List<ProcedureTypeDTO> getAllProceduresTypes() {
        return procedureTypeRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ProcedureTypeDTO::new)
                .toList();
    }


    @Transactional
    public ProcedureCategoryDTO updateServiceCategoryById(Long id, ProcedureCategoryUpdateRequest request) {

        return procedureCategoryRepository.findByIdOptional(id)
                .map(category -> {
                    // Update procedure fields

                    category.procedureCategory = request.procedureCategory;
                    category.categoryDescription = request.categoryDescription;

                    // Persist the updated procedure
                    procedureCategoryRepository.persist(category);

                    // Return the updated procedure as a DTO
                    return new ProcedureCategoryDTO(category);
                })
                .orElseThrow(() ->
                        new WebApplicationException("category not found for ID: " + id, Response.Status.NOT_FOUND)
                );
    }


    @Transactional
    public ProcedureTypeDTO updateServiceTypeById(Long id, ProcedureTypeUpdateRequest request) {

        return procedureTypeRepository.findByIdOptional(id)
                .map(type -> {
                    // Update procedure fields

                    type.procedureType = request.procedureType;
                    type.typeDescription = request.typeDescription;

                    // Persist the updated procedure
                    procedureTypeRepository.persist(type);

                    // Return the updated procedure as a DTO
                    return new ProcedureTypeDTO(type);
                })
                .orElseThrow(() ->
                        new WebApplicationException("type not found for ID: " + id, Response.Status.NOT_FOUND)
                );
    }




    @Transactional
    public Response createBulkProcedures(List<ProcedureRequest> requests) {
        List<ProcedureDTO> createdProcedures = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();

        for (ProcedureRequest request : requests) {
            boolean exists = Procedure.find(
                    "procedureType = ?1",

                    request.procedureType
            ).firstResultOptional().isPresent();

            if (exists) {
                duplicates.add("Procedure with name '" + request.procedureType
                        + "' and category '" + request.category + "' already exists.");
                continue;
            }

            Procedure procedure = new Procedure();
            procedure.procedureType = request.procedureType;
            procedure.procedureName = request.procedureName;
            procedure.category = request.category;
            procedure.description = request.description;
            procedure.unitSellingPrice = request.unitSellingPrice;
            procedure.unitCostPrice = request.unitCostPrice;

            procedureRepository.persist(procedure);

            createdProcedures.add(new ProcedureDTO(procedure));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("created", createdProcedures);
        result.put("duplicates", duplicates);

        return Response.ok(new ResponseMessage("Bulk procedure creation completed.", result)).build();
    }



    @Transactional
    public ProcedureDTO updateServiceById(Long id, ProcedureUpdateRequest request) {

        return procedureRepository.findByIdOptional(id)
                .map(procedure -> {
                    // Update procedure fields
                    procedure.procedureType = request.procedureType;
                    procedure.procedureName = request.procedureName;
                    procedure.category = request.category;
                    procedure.description = request.description;
                    procedure.unitSellingPrice = request.unitSellingPrice;
                    procedure.unitCostPrice = request.unitCostPrice;

                    // Persist the updated procedure
                    procedureRepository.persist(procedure);

                    // Return the updated procedure as a DTO
                    return new ProcedureDTO(procedure);
                })
                .orElseThrow(() ->
                        new WebApplicationException("Procedure not found for ID: " + id, Response.Status.NOT_FOUND)
                );
    }







    /**
     * Validates the LabTestRequest for mandatory fields.
     *
     * @param request LabTestRequest to validate
     */
    private void validateRequest(ProcedureRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(INVALID_REQUEST);
        }

        if (request.procedureType == null || request.procedureType.isBlank()) {
            throw new IllegalArgumentException("Test Type is required!");
        }

        if (request.unitCostPrice == null || request.unitSellingPrice == null) {
            throw new IllegalArgumentException("Cost and selling price are required!");
        }

        if (request.unitCostPrice.compareTo(request.unitSellingPrice) > 0) {
            throw new IllegalArgumentException("Cost price cannot be higher than selling price!");
        }
    }


    @Transactional
    public List<ProcedureDTO> getAllProcedures() {
        return procedureRepository.listAll(Sort.descending("id"))
                .stream()
                .map(ProcedureDTO::new)
                .toList();
    }

    public List<ProcedureDTO> getLabTestProcedures(){
        List<Procedure> labTests = Procedure.find(
                "category = ?1 ORDER BY id DESC",
                "labtest"
        ).list();

        // Convert ProcedureRequested entities to ProcedureDTO
        return labTests.stream()
                .map(ProcedureDTO::new)
                .collect(Collectors.toList());
    }

    public List<ProcedureDTO> getScanProcedures(){
        List<Procedure> scans = Procedure.find(
                "category = ?1 ORDER BY id DESC",
                "imaging"
        ).list();

        // Convert ProcedureRequested entities to ProcedureDTO
        return scans.stream()
                .map(ProcedureDTO::new)
                .collect(Collectors.toList());
    }


    public List<ProcedureDTO> getOtherProcedures() {
        List<Procedure> otherProcedures = Procedure.find(
                "category NOT IN (?1, ?2) ORDER BY id DESC",
                "imaging", "labtest"
        ).list();

        // Convert Procedure entities to ProcedureDTO
        return otherProcedures.stream()
                .map(ProcedureDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Response deleteServiceById(Long id){
        Procedure service = procedureRepository.findById(id);
        if (service == null) {
            //return Response.status(Response.Status.NOT_FOUND).build();
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("service not found", null))
                    .build();
        }
        procedureRepository.delete(service);
        return Response.ok(new ResponseMessage("Service Deleted successfully")).build();
    }






}
