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
import org.example.procedure.procedure.domains.repositories.ProcedureCategoryRepository;
import org.example.procedure.procedure.domains.repositories.ProcedureRepository;

import org.example.procedure.procedure.services.payloads.requests.*;
import org.example.procedure.procedure.services.payloads.responses.dtos.ProcedureCategoryDTO;
import org.example.procedure.procedure.services.payloads.responses.dtos.ProcedureDTO;

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
    ProcedureCategoryRepository procedureCategoryRepository;

    @Inject
    ItemRepository itemRepository;

    public static final String NOT_FOUND = "Lab test not found!";
    public static final String INVALID_REQUEST = "Invalid request data!";

    /* =========================================================
       CREATE PROCEDURE
       ========================================================= */
    @Transactional
    public Response createNewProcedure(ProcedureRequest request) {

        ProcedureCategory category =
                procedureCategoryRepository.findById(request.categoryId);

        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage(
                            "Category not found for ID: " + request.categoryId))
                    .build();
        }

        boolean exists = Procedure.find(
                "procedureName = ?1 and category.id = ?2",
                request.procedureName,
                category.id
        ).firstResultOptional().isPresent();

        if (exists) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "Procedure with name '" + request.procedureName +
                            "' and category '" + category.name + "' already exists.", null))
                    .build();
        }

        Procedure procedure = new Procedure();
        procedure.procedureName = request.procedureName;
        procedure.category = category;
        procedure.description = request.description;
        procedure.unitSellingPrice = request.unitSellingPrice;
        procedure.unitCostPrice = request.unitCostPrice;

        procedureRepository.persist(procedure);

        return Response.ok(
                new ResponseMessage(
                        "New Service created successfully",
                        new ProcedureDTO(procedure)))
                .build();
    }

    /* =========================================================
       GET ALL CATEGORIES
       ========================================================= */
    @Transactional
    public List<ProcedureCategoryDTO> getAllProceduresCategories() {
        return procedureCategoryRepository
                .listAll(Sort.descending("id"))
                .stream()
                .map(ProcedureCategoryDTO::new)
                .toList();
    }

    /* =========================================================
       BULK CREATE
       ========================================================= */
    @Transactional
    public Response createBulkProcedures(List<ProcedureRequest> requests) {

        List<ProcedureDTO> createdProcedures = new ArrayList<>();
        List<Map<String, String>> errors = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            ProcedureRequest request = requests.get(i);

            try {
                ProcedureCategory category =
                        procedureCategoryRepository.findById(request.categoryId);

                if (category == null) {
                    errors.add(Map.of(
                            "index", String.valueOf(i),
                            "procedureName", request.procedureName != null ? request.procedureName : "N/A",
                            "error", "Category not found for ID: " + request.categoryId
                    ));
                    continue;
                }

                boolean exists = Procedure.find(
                        "procedureName = ?1 and category.id = ?2",
                        request.procedureName,
                        category.id
                ).firstResultOptional().isPresent();

                if (exists) {
                    errors.add(Map.of(
                            "index", String.valueOf(i),
                            "procedureName", request.procedureName,
                            "error", "Procedure already exists in this category"
                    ));
                    continue;
                }

                Procedure procedure = new Procedure();
                procedure.procedureName = request.procedureName;
                procedure.category = category;
                procedure.description = request.description;
                procedure.unitSellingPrice = request.unitSellingPrice;
                procedure.unitCostPrice = request.unitCostPrice;

                procedureRepository.persist(procedure);
                createdProcedures.add(new ProcedureDTO(procedure));

            } catch (Exception e) {
                errors.add(Map.of(
                        "index", String.valueOf(i),
                        "procedureName", request.procedureName != null ? request.procedureName : "N/A",
                        "error", e.getMessage()
                ));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("created", createdProcedures);
        result.put("createdCount", createdProcedures.size());
        result.put("errors", errors);
        result.put("errorCount", errors.size());
        result.put("totalProcessed", requests.size());

        if (errors.isEmpty()) {
            return Response.ok(
                    new ResponseMessage(
                            "All procedures created successfully",
                            result))
                    .build();
        }

        if (createdProcedures.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage(
                            "No procedures created",
                            result))
                    .build();
        }

        return Response.status(Response.Status.PARTIAL_CONTENT)
                .entity(new ResponseMessage(
                        "Some procedures failed",
                        result))
                .build();
    }

    /* =========================================================
       UPDATE PROCEDURE  (❗ FIXED)
       ========================================================= */
    @Transactional
    public ProcedureDTO updateProcedureById(Long id, ProcedureUpdateRequest request) {

        Procedure procedure = procedureRepository.findById(id);
        if (procedure == null) {
            throw new WebApplicationException(
                    "Procedure not found for ID: " + id,
                    Response.Status.NOT_FOUND);
        }

        // Determine the final category (new one if provided, otherwise keep existing)
        ProcedureCategory finalCategory = procedure.category;
        if (request.categoryId != null) {
            ProcedureCategory category =
                    procedureCategoryRepository.findById(request.categoryId);

            if (category == null) {
                throw new WebApplicationException(
                        "Category not found for ID: " + request.categoryId,
                        Response.Status.NOT_FOUND);
            }

            finalCategory = category;
        }

        // Check for duplicate procedure name in the final category
        if (request.procedureName != null && !request.procedureName.isBlank()) {
            boolean exists;
            if (finalCategory != null) {
                exists = Procedure.find(
                        "procedureName = ?1 and category.id = ?2 and id != ?3",
                        request.procedureName,
                        finalCategory.id,
                        id
                ).firstResultOptional().isPresent();
            } else {
                // If final category is null, check for duplicates with no category
                exists = Procedure.find(
                        "procedureName = ?1 and category is null and id != ?2",
                        request.procedureName,
                        id
                ).firstResultOptional().isPresent();
            }

            if (exists) {
                throw new WebApplicationException(
                        "Procedure with same name already exists",
                        Response.Status.BAD_REQUEST);
            }

            procedure.procedureName = request.procedureName;
        }

        // Update category if provided
        if (request.categoryId != null) {
            procedure.category = finalCategory;
        }

        if (request.description != null) {
            procedure.description = request.description;
        }

        if (request.unitSellingPrice != null) {
            procedure.unitSellingPrice = request.unitSellingPrice;
        }

        if (request.unitCostPrice != null) {
            procedure.unitCostPrice = request.unitCostPrice;
        }

        // ❌ persist() REMOVED — entity already managed
        return new ProcedureDTO(procedure);
    }

    /* =========================================================
       GET ALL PROCEDURES
       ========================================================= */
    @Transactional
    public List<ProcedureDTO> getAllProcedures() {
        return procedureRepository
                .listAll(Sort.descending("id"))
                .stream()
                .map(ProcedureDTO::new)
                .toList();
    }

    /* =========================================================
       CATEGORY-BASED LISTING (FIXED)
       ========================================================= */
    public List<ProcedureDTO> getLabTestProcedures() {
        return getByCategoryCode("labtest");
    }

    public List<ProcedureDTO> getScanProcedures() {
        return getByCategoryCode("imaging");
    }

    public List<ProcedureDTO> getOtherProcedures() {

        ProcedureCategory lab =
                procedureCategoryRepository
                        .find("procedureCategory", "labtest")
                        .firstResult();

        ProcedureCategory imaging =
                procedureCategoryRepository
                        .find("procedureCategory", "imaging")
                        .firstResult();

        if (lab == null || imaging == null) {
            return List.of();
        }

        return Procedure.find(
                "category.id NOT IN (?1, ?2) ORDER BY id DESC",
                lab.id, imaging.id
        ).<Procedure>list()
         .stream()
         .map(ProcedureDTO::new)
         .toList();
    }

    private List<ProcedureDTO> getByCategoryCode(String code) {

        ProcedureCategory category =
                procedureCategoryRepository
                        .find("procedureCategory", code)
                        .firstResult();

        if (category == null) {
            return List.of();
        }

        return Procedure.find(
                "category.id = ?1 ORDER BY id DESC",
                category.id
        ).<Procedure>list()
         .stream()
         .map(ProcedureDTO::new)
         .toList();
    }

    /* =========================================================
       DELETE
       ========================================================= */
    @Transactional
    public Response deleteServiceById(Long id) {

        Procedure service = procedureRepository.findById(id);
        if (service == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("service not found", null))
                    .build();
        }

        procedureRepository.delete(service);
        return Response.ok(
                new ResponseMessage("Service Deleted successfully"))
                .build();
    }
}
