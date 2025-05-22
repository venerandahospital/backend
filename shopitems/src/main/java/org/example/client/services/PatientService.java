package org.example.client.services;

import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.repositories.PatientRepository;
import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;
import org.example.client.domains.repositories.PatientGroupRepository;
import org.example.client.services.payloads.requests.PatientParametersRequest;
import org.example.client.services.payloads.requests.PatientRequest;
import org.example.client.services.payloads.requests.PatientUpdateRequest;
import org.example.client.services.payloads.responses.FullPatientResponse;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class PatientService {

    @Inject
    PatientRepository patientRepository;

    @Inject
    DeletedPatientNosService deletedPatientNosService;

    @Inject
    PatientGroupRepository patientGroupRepository;

    @Inject
    MySQLPool client;


    public static final String NOT_FOUND = "Not found!";

    @Transactional
    public Response createNewPatient(PatientRequest request) {
        // Validate the request
        /*if (request.patientAge == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Patient Age cannot be null or empty", null))
                    .build();
        }

        if (request.patientGender == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Patient Gender cannot be null or empty", null))
                    .build();
        }*/




        // Check if a patient with the same first and second names already exists
        Patient existingPatient = patientRepository.findByFirstNameAndSecondName(
                request.patientFirstName, request.patientSecondName);

        if (existingPatient != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("A patient with the same first and second names already exists", null))
                    .build();
        }



        PatientGroup patientGroup = null;
        if (request.patientGroupId != null) {
            patientGroup = patientGroupRepository.findById(request.patientGroupId);
            if (patientGroup == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Patient group not found for ID: " + request.patientGroupId, null))
                        .build();
            }
        }

        // Create new Patient entity and set basic information
        Patient patient = new Patient();
        patient.patientGroup = patientGroup;
        patient.patientFirstName = request.patientFirstName;
        patient.patientSecondName = request.patientSecondName;
        patient.patientAddress = request.patientAddress;
        patient.patientAge = request.patientAge;
        patient.patientContact = request.patientContact;
        patient.patientGender = request.patientGender;

//        patient.patientProfilePic = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2Fplaceholder.jpg?alt=media&token=caade802-c591-4dee-b590-a040c694553b";

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

        // Generate patient file number
        patient.patientFileNo = "VMD" + patient.patientNo;

        // Persist the new Patient entity
        patientRepository.persist(patient);

        // Remove the deleted patient number from the queue
        if (deletedPatientNumberInQue != 0) {
            deletedPatientNosService.deleteByDeletedPatientNumber(deletedPatientNumberInQue);
        }

        // Return a success response with the created PatientDTO
        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient created successfully", new PatientDTO(patient)))
                .build();
    }


    public void updateTotalAmountDue(Patient patient, BigDecimal totalAmountDue){

        patient.totalAmountDue = totalAmountDue;

        patientRepository.persist(patient);

    }


    @Transactional
    public Response createMultiplePatients(List<PatientRequest> requests) {
        List<PatientDTO> createdPatients = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (PatientRequest request : requests) {
            try {

                // Check for existing patient
                Patient existingPatient = patientRepository.findByFirstNameAndSecondName(
                        request.patientFirstName, request.patientSecondName);
                if (existingPatient != null) {
                    errors.add("Duplicate patient: " + request.patientFirstName + " " + request.patientSecondName);
                    continue;
                }

                // Check patient group
                PatientGroup patientGroup = null;
                if (request.patientGroupId != null) {
                    patientGroup = patientGroupRepository.findById(request.patientGroupId);
                    if (patientGroup == null) {
                        errors.add("Invalid group ID for patient: " + request.patientFirstName + " " + request.patientSecondName);
                        continue;
                    }
                }

                // Create and populate Patient
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

                // Set next of kin info
                patient.nextOfKinName = request.nextOfKinName;
                patient.nextOfKinAddress = request.nextOfKinAddress;
                patient.nextOfKinContact = request.nextOfKinContact;
                patient.relationship = request.relationship;

                // Assign patient number
                int deletedPatientNumberInQue = deletedPatientNosService.findFirstDeletedPatientNo();
                if (deletedPatientNumberInQue == 0) {
                    patient.patientNo = findMaxPatientFileNoReturnInt() + 1;
                } else {
                    patient.patientNo = deletedPatientNumberInQue;
                }

                patient.patientFileNo = "VMD" + patient.patientNo;

                patientRepository.persist(patient);

                // Remove number from deleted queue
                if (deletedPatientNumberInQue != 0) {
                    deletedPatientNosService.deleteByDeletedPatientNumber(deletedPatientNumberInQue);
                }

                createdPatients.add(new PatientDTO(patient));

            } catch (Exception ex) {
                errors.add("Error creating patient: " + request.patientFirstName + " " + request.patientSecondName);
            }
        }

        if (createdPatients.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("No patients created", errors))
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patients created successfully", createdPatients))
                .build();
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

    @Transactional
    public List<PatientDTO> getAllPatientsWithDebt() {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .filter(patient -> patient.getTotalBalanceDue() != null &&
                        patient.getTotalBalanceDue().compareTo(BigDecimal.ZERO) > 0)
                .map(PatientDTO::new)
                .toList();
    }

    @Transactional
    public List<PatientDTO> getAllPatientsByGroupId(Long groupId) {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .filter(patient -> patient.patientGroup != null &&
                        patient.patientGroup.id.equals(groupId))
                .map(PatientDTO::new)
                .toList();
    }


    public PatientDTO getPatientById(Long id) {
        return patientRepository.findByIdOptional(id)
                .map(PatientDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("Patient not found", 404));
    }

    @Transactional
    public PatientDTO updatePatientById(Long id, PatientUpdateRequest request) {
        // Find the patient group (only if patientGroupId is provided)
        PatientGroup patientGroup;
        if (request.patientGroupId != null) {
            patientGroup = patientGroupRepository.findById(request.patientGroupId);
            if (patientGroup == null) {
                throw new IllegalArgumentException("Patient group not found for ID: " + request.patientGroupId);
            }
        } else {
            patientGroup = null;
        }

        return patientRepository.findByIdOptional(id)
                .map(patient -> {
                    // Update patient fields
                    patient.patientFirstName = request.patientFirstName;
                    patient.patientSecondName = request.patientSecondName;
                    patient.patientAddress = request.patientAddress;
                    patient.patientContact = request.patientContact;
                    patient.patientGender = request.patientGender;
                    patient.patientAge = request.patientAge;
                    patient.patientGroup = patientGroup; // Can be null
                    patient.nextOfKinName = request.nextOfKinName;
                    patient.nextOfKinContact = request.nextOfKinContact;
                    patient.relationship = request.relationship;
                    patient.nextOfKinAddress = request.nextOfKinAddress;
                    patient.patientDateOfBirth = request.patientDateOfBirth;
                    patient.patientLastUpdatedDate = LocalDate.now();

                    // Persist the updated patient
                    patientRepository.persist(patient);

                    // Return the updated patient as a DTO
                    return new PatientDTO(patient);
                })
                .orElseThrow(() -> new WebApplicationException("Patient not found for ID: " + id, Integer.parseInt(NOT_FOUND)));
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





    public List<FullPatientResponse> getPatientsAdvancedFilter(PatientParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
        SELECT
            id,
            group_id,
            nextOfKinAddress,
            nextOfKinContact,
            nextOfKinName,
            patientAddress,
            patientAge,
            patientContact,
            patientDateOfBirth,
            patientFileNo,
            patientFirstName,
            patientGender,
            patientLastUpdatedDate,
            patientNo,
            patientProfilePic,
            patientSecondName,
            relationship,
            totalAmountDue
        FROM vena.Patient
        %s
        ORDER BY id DESC;
        """.formatted(whereClause);

        return client.query(sql)
                .execute()
                .onItem()
                .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem()
                .transform(this::from)
                .collect().asList()
                .await()
                .indefinitely();
    }


    private FullPatientResponse from(Row row){

        FullPatientResponse response = new FullPatientResponse();
        response.id = row.getLong("id");
        response.group_id = row.getLong("group_id");
        response.nextOfKinAddress = row.getString("nextOfKinAddress");
        response.nextOfKinContact = row.getString("nextOfKinContact");
        response.nextOfKinName = row.getString("nextOfKinName");
        response.patientAddress = row.getString("patientAddress");
        response.patientAge = row.getBigDecimal("patientAge");
        response.totalAmountDue = row.getBigDecimal("totalAmountDue");
        response.patientNo = row.getInteger("patientNo");

        response.patientContact = row.getString("patientContact");
        response.patientFileNo = row.getString("patientFileNo");
        response.patientFirstName = row.getString("patientFirstName");
        response.patientGender = row.getString("patientGender");
        response.patientProfilePic = row.getString("patientProfilePic");
        response.patientSecondName = row.getString("patientSecondName");


        response.patientDateOfBirth = row.getLocalDate("patientDateOfBirth");
        response.patientLastUpdatedDate = row.getLocalDate("patientLastUpdatedDate");

        return response;
    }

    private StringJoiner getStringJoiner(PatientParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.group_id != null) {
            conditions.add("group_id = " + request.group_id);
            hasSearchCriteria.set(Boolean.TRUE);
        }


        if (request.patientAddress != null && !request.patientAddress.isEmpty()) {
            conditions.add("patientAddress = '" + request.patientAddress + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.patientGender != null && !request.patientGender.isEmpty()) {
            conditions.add("patientGender = '" + request.patientGender + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        /*if (request.datefrom != null && request.dateto != null) {
            conditions.add("dateOfPayment BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }*/

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }









}
