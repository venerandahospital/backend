package org.example.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import org.example.domains.Invoice;
import org.example.domains.PatientGroup;
import org.example.domains.repositories.PatientGroupRepository;
import org.example.services.payloads.requests.PatientGroupRequest;
import org.example.services.payloads.requests.PatientGroupUpdateRequest;

import org.example.services.payloads.responses.dtos.PatientDTO;
import org.example.services.payloads.responses.dtos.PatientGroupDTO;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class PatientGroupService {

    @Inject
    PatientGroupRepository patientGroupRepository;

    private static final String NOT_FOUND = "Not found!";


    @Transactional
    public PatientGroupDTO createNewPatientGroup(PatientGroupRequest request){

        PatientGroup patientGroup = new PatientGroup();
        patientGroup.groupName = request.groupName;
        patientGroup.groupEmail = request.groupEmail;
        patientGroup.groupContact = request.groupContact;
        patientGroup.groupAddress = request.groupAddress;
        patientGroup.description = request.description;
        patientGroup.groupNameShortForm = request.groupNameShortForm;
        patientGroup.patientGroupCreationDate = LocalDate.now();

        patientGroupRepository.persist(patientGroup);

        return new PatientGroupDTO(patientGroup);
    }

    @Transactional
    public List<PatientGroupDTO> getAllPatientGroups() {
        return patientGroupRepository.listAll(Sort.descending("id"))
                .stream()
                .map(PatientGroupDTO::new)
                .toList();
    }

    public PatientGroupDTO updatePatientGroupById(Long id, PatientGroupUpdateRequest request) {
        return patientGroupRepository.findByIdOptional(id)
                .map(patientGroup -> {

                    patientGroup.groupName = request.groupName;
                    patientGroup.groupNameShortForm = request.groupNameShortForm;
                    patientGroup.groupAddress = request.groupAddress;
                    patientGroup.groupEmail = request.groupEmail;
                    patientGroup.groupContact = request.groupContact;
                    patientGroup.patientGroupLastUpdatedDate = LocalDate.now();

                    patientGroupRepository.persist(patientGroup);

                    return new PatientGroupDTO(patientGroup);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

    public PatientGroupDTO getPatientGroupById(Long id) {
        return patientGroupRepository.findByIdOptional(id)
                .map(PatientGroupDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("Patient Group not found", 404));
    }







}
