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
import org.example.client.domains.DeletedPatient;
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
import org.example.treatment.services.payloads.responses.TreatmentRequestedDTO;

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

        // Check if a buyer with the same first and second names already exists
        Patient existingPatient = patientRepository.findByFirstNameAndSecondName(
                request.patientFirstName, request.patientSecondName);

        if (existingPatient != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("A buyer with the same first and second names already exists", null))
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

            // Check if group is "veneranda medical" and role is not "md"
            if ("veneranda medical".equalsIgnoreCase(patientGroup.groupName) &&
                    (request.userRole == null || !request.userRole.equalsIgnoreCase("md"))) {

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ResponseMessage("You need admin approval to add any patient in Veneranda Medical group", null))
                        .build();
            }
        }


        // Create new Patient entity and set basic information
        Patient buyer = new Patient();
        buyer.patientGroup = patientGroup;
        buyer.patientFirstName = request.patientFirstName;
        buyer.patientSecondName = request.patientSecondName;
        buyer.patientAddress = request.patientAddress;
        buyer.patientAge = request.patientAge;
        buyer.patientContact = request.patientContact;
        buyer.patientGender = request.patientGender;
        buyer.occupation = request.occupation;

//      buyer.patientProfilePic = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2Fplaceholder.jpg?alt=media&token=caade802-c591-4dee-b590-a040c694553b";

        buyer.patientDateOfBirth = request.patientDateOfBirth;
        buyer.creationDate = LocalDate.now();

        // Set Next of Kin information
        buyer.nextOfKinName = request.nextOfKinName;
        buyer.nextOfKinAddress = request.nextOfKinAddress;
        buyer.nextOfKinContact = request.nextOfKinContact;
        buyer.relationship = request.relationship;

// Determine buyer number
       // DeletedPatient deletedPatientInQue = deletedPatientNosService.findFirstDeletedPatient();

        buyer.patientNo = patientRepository.generateNextPatientNo();


        // Generate buyer file number
        buyer.patientFileNo = "VMD" + buyer.patientNo;

        // Persist the new Patient entity
        patientRepository.persist(buyer);

        // Remove the deleted buyer number from the queue
        /*if (deletedPatientInQue.patientNo != 0) {
            deletedPatientNosService.deleteByDeletedPatient(deletedPatientInQue);
        }*/

        // Return a success response with the created PatientDTO
        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Patient created successfully", new PatientDTO(buyer)))
                .build();
    }


    public void updateTotalAmountDue(Patient buyer, BigDecimal totalAmountDue){

        buyer.totalAmountDue = totalAmountDue;

        patientRepository.persist(buyer);

    }


    @Transactional
    public Response createMultiplePatients(List<PatientRequest> requests) {
        List<PatientDTO> createdPatients = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (PatientRequest request : requests) {
            try {

                // Check for existing buyer
                Patient existingPatient = patientRepository.findByFirstNameAndSecondName(
                        request.patientFirstName, request.patientSecondName);
                if (existingPatient != null) {
                    errors.add("Duplicate buyer: " + request.patientFirstName + " " + request.patientSecondName);
                    continue;
                }

                // Check buyer group
                PatientGroup patientGroup = null;
                if (request.patientGroupId != null) {
                    patientGroup = patientGroupRepository.findById(request.patientGroupId);

                    if (patientGroup == null) {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity(new ResponseMessage("Patient group not found for ID: " + request.patientGroupId, null))
                                .build();
                    }

                    // Check if group is "veneranda medical" and role is not "md"
                    if ("veneranda medical".equalsIgnoreCase(patientGroup.groupName) &&
                            (request.userRole == null || !request.userRole.equalsIgnoreCase("md"))) {

                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(new ResponseMessage("You need admin approval to add any patient in Veneranda Medical group", null))
                                .build();
                    }
                }

                // Create and populate Patient
                Patient buyer = new Patient();
                buyer.patientGroup = patientGroup;
                buyer.patientFirstName = request.patientFirstName;
                buyer.patientSecondName = request.patientSecondName;
                buyer.patientAddress = request.patientAddress;
                buyer.patientAge = request.patientAge;
                buyer.patientContact = request.patientContact;
                buyer.patientGender = request.patientGender;
                buyer.patientProfilePic = request.patientProfilePic;
                buyer.patientDateOfBirth = request.patientDateOfBirth;
                buyer.creationDate = LocalDate.now();

                // Set next of kin info
                buyer.nextOfKinName = request.nextOfKinName;
                buyer.occupation = request.occupation;
                buyer.nextOfKinAddress = request.nextOfKinAddress;
                buyer.nextOfKinContact = request.nextOfKinContact;
                buyer.relationship = request.relationship;

                // Assign buyer number
                // Determine buyer number
               /* DeletedPatient deletedPatientInQue = deletedPatientNosService.findFirstDeletedPatient();

                if (deletedPatientInQue != null) {
                    buyer.patientNo = deletedPatientInQue.patientNo;
                    //deletedPatientNosService.delete(deletedPatientInQue); // remove used number
                } else {
                    buyer.patientNo = patientRepository.generateNextPatientNo();
                }*/

                buyer.patientNo = patientRepository.generateNextPatientNo();

                buyer.patientFileNo = "VMD" + buyer.patientNo;

                patientRepository.persist(buyer);

                // Remove number from deleted queue
                /*assert deletedPatientInQue != null;
                if (deletedPatientInQue.patientNo != 0) {
                    deletedPatientNosService.deleteByDeletedPatient(deletedPatientInQue);
                }*/

                createdPatients.add(new PatientDTO(buyer));

            } catch (Exception ex) {
                errors.add("Error creating buyer: " + request.patientFirstName + " " + request.patientSecondName);
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
                .filter(buyer -> buyer.getTotalBalanceDue() != null &&
                        buyer.getTotalBalanceDue().compareTo(BigDecimal.ZERO) > 0)
                .map(PatientDTO::new)
                .toList();
    }

    @Transactional
    public List<PatientDTO> getAllPatientsByGroupId(Long groupId) {
        return patientRepository.listAll(Sort.descending("patientNo"))
                .stream()
                .filter(buyer -> buyer.patientGroup != null &&
                        buyer.patientGroup.id.equals(groupId))
                .map(PatientDTO::new)
                .toList();
    }


    public PatientDTO getPatientById(Long id) {
        return patientRepository.findByIdOptional(id)
                .map(PatientDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("Patient not found", 404));
    }



    @Transactional
    public Response updatePatientById(Long id, PatientUpdateRequest request) {

                    PatientGroup patientGroup = null;

                    // Validate and assign patient group
                    if (request.patientGroupId != null) {
                        patientGroup = patientGroupRepository.findById(request.patientGroupId);
                        if (patientGroup == null) {
                            throw new WebApplicationException(
                                    Response.status(Response.Status.NOT_FOUND)
                                            .entity(new ResponseMessage("Patient group not found for ID: " + request.patientGroupId, null))
                                            .build()
                            );
                        }

                        if ("veneranda medical".equalsIgnoreCase(patientGroup.groupName) &&
                                (request.userRole == null || !request.userRole.equalsIgnoreCase("md"))) {
                            throw new WebApplicationException(
                                    Response.status(Response.Status.BAD_REQUEST)
                                            .entity(new ResponseMessage("You need admin approval to add any patient in Veneranda Medical group", null))
                                            .build()
                            );
                        }
                    }

                    Patient patient = patientRepository.findById(id);
                    // Update patient fields
                    patient.patientFirstName = request.patientFirstName;
                    patient.patientSecondName = request.patientSecondName;
                    patient.patientAddress = request.patientAddress;
                    patient.patientContact = request.patientContact;
                    patient.patientGender = request.patientGender;
                    patient.patientAge = request.patientAge;
                    patient.occupation = request.occupation;
                    patient.patientGroup = patientGroup;
                    patient.nextOfKinName = request.nextOfKinName;
                    patient.nextOfKinContact = request.nextOfKinContact;
                    patient.relationship = request.relationship;
                    patient.nextOfKinAddress = request.nextOfKinAddress;
                    patient.patientDateOfBirth = request.patientDateOfBirth;
                    patient.patientLastUpdatedDate = LocalDate.now();

                    patientRepository.persist(patient);


        return Response.ok(new ResponseMessage("New treatment request created successfully", new PatientDTO(patient))).build();


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
                .map(buyer -> buyer.patientNo)
                .findFirst()
                .orElse(0);
    }

    @Transactional
    public Response deletePatientById(Long id) {

        Patient deletedPatient = patientRepository.findById(id);

        if (deletedPatient == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        deletedPatientNosService.saveDeletedPatientNo(deletedPatient);
        
        patientRepository.delete(deletedPatient);

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
