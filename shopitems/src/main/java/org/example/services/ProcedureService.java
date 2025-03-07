package org.example.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.domains.Procedure;
import org.example.domains.repositories.ItemRepository;
import org.example.domains.repositories.ProcedureRepository;
import org.example.services.payloads.requests.ProcedureRequest;
import org.example.services.payloads.responses.dtos.ProcedureDTO;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcedureService {

    @Inject
    ProcedureRepository procedureRepository;

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
    public ProcedureDTO createNewProcedure(ProcedureRequest request) {

        // Check if a procedure with the same name and type already exists
        boolean exists = Procedure.find(
                "procedureName = ?1 and procedureType = ?2",
                request.procedureName,
                request.procedureType
        ).firstResultOptional().isPresent();

        if (exists) {
            throw new IllegalArgumentException("Procedure with name '" + request.procedureName
                    + "' and type '" + request.procedureType + "' already exists.");
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

        return new ProcedureDTO(procedure);
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

        if (request.procedureName == null || request.procedureName.isBlank()) {
            throw new IllegalArgumentException("Test name is required!");
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
                "procedureType = ?1 ORDER BY id DESC",
                "LabTest"
        ).list();

        // Convert ProcedureRequested entities to ProcedureDTO
        return labTests.stream()
                .map(ProcedureDTO::new)
                .collect(Collectors.toList());
    }

    public List<ProcedureDTO> getScanProcedures(){
        List<Procedure> scans = Procedure.find(
                "procedureType = ?1 ORDER BY id DESC",
                "imaging"
        ).list();

        // Convert ProcedureRequested entities to ProcedureDTO
        return scans.stream()
                .map(ProcedureDTO::new)
                .collect(Collectors.toList());
    }


    public List<ProcedureDTO> getOtherProcedures() {
        List<Procedure> otherProcedures = Procedure.find(
                "procedureType NOT IN (?1, ?2) ORDER BY id DESC",
                "imaging", "LabTest"
        ).list();

        // Convert Procedure entities to ProcedureDTO
        return otherProcedures.stream()
                .map(ProcedureDTO::new)
                .collect(Collectors.toList());
    }






}
