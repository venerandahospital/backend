package org.example.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.Patient;
import org.example.domains.PatientGroup;
import org.example.domains.repositories.PatientGroupRepository;
import org.example.domains.repositories.PatientRepository;
import org.example.services.payloads.requests.PatientRequest;
import org.example.services.payloads.requests.PatientUpdateRequest;
import org.example.services.payloads.responses.dtos.PatientDTO;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class PatientService {

    @Inject
    PatientRepository patientRepository;

    @Inject
    DeletedPatientNosService deletedPatientNosService;

    @Inject
    PatientGroupRepository patientGroupRepository;


    public static final String NOT_FOUND = "Not found!";

    @Transactional
    public PatientDTO createNewPatient(PatientRequest request) {
        // Create new Patient entity and set basic information
        PatientGroup patientGroup = patientGroupRepository.findById(request.patientGroupId);
        if (patientGroup == null) {
            throw new IllegalArgumentException("patientGroup not found for ID: " + request.patientGroupId);
        }

        Patient patient = new Patient();
        patient.patientGroup = patientGroup;
        patient.patientFirstName = request.patientFirstName;
        patient.patientSecondName = request.patientSecondName;
        patient.patientAddress = request.patientAddress;
        patient.patientAge = request.patientAge;
        patient.patientContact = request.patientContact;
        patient.patientGender = request.patientGender;
        patient.patientProfilePic = request.patientProfilePic;
        patient.patientDateOfBirth = request.patientDateOfBirth;
        patient.creationDate = LocalDate.now();

        // Set Next of Kin information
        patient.nextOfKinName = request.nextOfKinName;
        patient.nextOfKinAddress = request.nextOfKinAddress;
        patient.nextOfKinContact = request.nextOfKinContact;
        patient.relationship = request.relationship;

        // Determine patient number
        int deletedPatientNumberInQue = deletedPatientNosService.findFirstDeletedPatientNo();
        if (deletedPatientNumberInQue == 0) {
            patient.patientNo = findMaxPatientFileNoReturnInt() + 1;
        } else {
            patient.patientNo = deletedPatientNumberInQue;
        }

        patient.patientFileNo = "VMD" + patient.patientNo;

        // Persist the new Patient entity
        patientRepository.persist(patient);

        // Remove the deleted patient number from the queue
        deletedPatientNosService.deleteByDeletedPatientNumber(deletedPatientNumberInQue);

        // Return a new PatientDTO from the saved Patient entity
        return new PatientDTO(patient);
    }
    /*@Transactional
    public List<Patient> getAllPatients() {
        return patientRepository.listAll(Sort.descending("patientNo"));
    }*/

    @Transactional
    public List<PatientDTO> getAllPatients() {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .map(PatientDTO::new)
                .toList();
    }


    public PatientDTO getPatientById(Long id) {
        return patientRepository.findByIdOptional(id)
                .map(PatientDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("Patient not found", 404));
    }

    public PatientDTO updatePatientById(Long id, PatientUpdateRequest request) {

        PatientGroup patientGroup = patientGroupRepository.findById(request.patientGroupId);
        if (patientGroup == null) {
            throw new IllegalArgumentException("patientGroup not found for ID: " + request.patientGroupId);
        }

        return patientRepository.findByIdOptional(id)
                .map(patient -> {

                    patient.patientFirstName = request.patientFirstName;
                    patient.patientSecondName = request.patientSecondName;
                    patient.patientAddress = request.patientAddress;
                    patient.patientContact = request.patientContact;
                    patient.patientGender = request.patientGender;
                    patient.patientAge = request.patientAge;
                    patient.patientGroup = patientGroup;
                    patient.nextOfKinName = request.nextOfKinName;
                    patient.nextOfKinContact = request.nextOfKinContact;
                    patient.relationship = request.relationship;
                    patient.nextOfKinAddress = request.nextOfKinAddress;
                    patient.patientDateOfBirth = request.patientDateOfBirth;
                    patient.patientLastUpdatedDate = LocalDate.now();

                    patientRepository.persist(patient);

                    return new PatientDTO(patient);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }


    @Transactional
    public Object findMaxPatientNo() {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .findFirst()
                .orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }

   // public static final String NOT_FOUND = "Not found!";


    @Transactional
    public int findMaxPatientFileNoReturnInt() {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .map(patient -> patient.patientNo)
                .findFirst()
                .orElse(0);
    }

    @Transactional
    public Response deletePatientById(Long id) {

        Patient patient = patientRepository.findById(id);

        if (patient == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        deletedPatientNosService.saveDeletedPatientNo(patient.patientNo);
        
        patientRepository.delete(patient);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }












}
