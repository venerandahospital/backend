package org.example.procedure.procedureRequested.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.GeneralUs;
import org.example.diagnostics.ultrasoundScan.generalUs.services.GeneralUsService;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.services.InvoiceService;
import org.example.finance.invoice.services.payloads.requests.InvoiceUpdateRequest;
import org.example.procedure.itemUsedInProcedure.services.ItemUsedService;
import org.example.procedure.procedure.domains.Procedure;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.procedure.procedureRequested.services.payloads.requests.ProcedureRequestedRequest;
import org.example.procedure.procedureRequested.services.payloads.requests.ProcedureRequestedUpdateRequest;
import org.example.procedure.procedureRequested.services.payloads.responses.ProcedureRequestedDTO;
import org.example.visit.domains.PatientVisit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProcedureRequestedService {
    @Inject
    ProcedureRequestedRepository proceduresRequestedRepository;

    @Inject
    InvoiceService invoiceService;

    @Inject
    ItemUsedService itemUsedService;

    @Inject
    GeneralUsService generalUsService;

    public static final String NOT_FOUND = "Not found!";
    public static final String VISIT_CLOSED = "Not found!";

    public Response createNewProcedureRequested(Long visitID, ProcedureRequestedRequest request) {
        // Fetch the PatientVisit and Procedure in one go
        PatientVisit patientVisit = PatientVisit.findById(visitID);

        if (patientVisit == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Patient visit not found for ID: " + visitID, null))
                    .build();
        }

        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Please Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }

        // Fetch procedure
        Procedure procedure = Procedure.findById(request.procedureId);
        if (procedure == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Invalid procedure ID: " + request.procedureId, null))
                    .build();
        }

        // Check if it's a consultation category
        if ("consultation".equalsIgnoreCase(procedure.category)) {
            boolean consultationAlreadyDone = proceduresRequestedRepository
                    .find("category = ?1 and visit.id = ?2", "consultation", visitID)
                    .firstResultOptional()
                    .isPresent();

            if (consultationAlreadyDone) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("Consultation already done for this visit", null))
                        .build();
            }
        }




        BigDecimal totalBalanceDue = invoiceService.calculateTotalBalanceDueForClosedVisits(patientVisit.patient.id);

        if (totalBalanceDue.compareTo(BigDecimal.ZERO) > 0 &&
                (patientVisit.patient.patientGroup == null || !patientVisit.patient.patientGroup.groupName.equalsIgnoreCase("veneranda medical"))) {

            // There is an unpaid balance and the patient is not part of the "family" group
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Cannot access any service. Patient has a debt of: "
                            + totalBalanceDue + " and doesn't belong to veneranda medical group"+". Please clear the debt first. or Contact Admin"))
                    .build();
        }


        // Throw exception if either of them is not found now


        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }


        // Throw exception if either of them is not found now

        if (procedure.category == null) {
            //throw new IllegalArgumentException(NOT_FOUND); // Handle not found error
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage(procedure.procedureName + " " + "Service has no category, please give it a category and try again", null))
                    .build();
        }


        // Check if a ProcedureRequested with the same procedure and visit already exists
        ProcedureRequested existingProcedureRequested = ProcedureRequested.find(
                "visit.id = ?1 and procedureRequestedType  = ?2 and unitSellingPrice = ?3",
                patientVisit.id,
                procedure.procedureType,
                request.unitSellingPrice
        ).firstResult();

        if (existingProcedureRequested != null) {

            /*
            existingProcedureRequested.quantity += 1;
            existingProcedureRequested.totalAmount = BigDecimal.valueOf(existingProcedureRequested.quantity)
                    .multiply(request.unitSellingPrice);

            proceduresRequestedRepository.persist(existingProcedureRequested); // Persist the updated entity

            itemUsedService.performProcedure(request.procedureId);*/


            //return new ProcedureRequestedDTO(existingProcedureRequested);
            return Response.ok(new ResponseMessage("procedure already requested", new ProcedureRequestedDTO(existingProcedureRequested))).build();

        } else {
            // Otherwise, create a new ProcedureRequested record
            ProcedureRequested procedureRequested = new ProcedureRequested();
            procedureRequested.patientName = patientVisit.patient.patientFirstName+" "+patientVisit.patient.patientSecondName;
            procedureRequested.quantity = request.quantity;
            procedureRequested.report = request.report;
            procedureRequested.procedureId = request.procedureId;
            procedureRequested.orderedBy = request.orderedBy;
            procedureRequested.doneBy = request.doneBy;
            procedureRequested.unitSellingPrice = request.unitSellingPrice;
            procedureRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(request.unitSellingPrice);
            procedureRequested.visit = patientVisit;
            procedureRequested.procedureRequestedType = procedure.procedureType;
            procedureRequested.exam = procedure.procedureType;
            procedureRequested.status = "pending";

            procedureRequested.category = procedure.category;
            procedureRequested.dateOfProcedure = java.time.LocalDate.now();
            procedureRequested.timeOfProcedure = java.time.LocalTime.now();

            proceduresRequestedRepository.persist(procedureRequested);

            itemUsedService.performProcedure(request.procedureId);

            if(Objects.equals(procedureRequested.category, "imaging")){
                generalUsService.createGeneralUsReport(procedureRequested);

            }


            //return new ProcedureRequestedDTO(procedureRequested);
            return Response.ok(new ResponseMessage("New procedure request made successfully", new ProcedureRequestedDTO(procedureRequested))).build();

        }
    }



    public ProcedureRequestedDTO getOtherRequestedProcedureById(Long id) {
        return proceduresRequestedRepository.findByIdOptional(id)
                .map(ProcedureRequestedDTO::new)  // Convert Patient entity to PatientDTO
                .orElseThrow(() -> new WebApplicationException("ProcedureRequested not found", 404));
    }

    public List<ProcedureRequestedDTO> getRequestedProceduresByVisitId(Long visitId) {
        List<ProcedureRequested> requestedProcedures = proceduresRequestedRepository
                .find("visit.id = ?1 ORDER BY id DESC", visitId)
                .list();

        return requestedProcedures.stream()
                .map(ProcedureRequestedDTO::new)
                .collect(Collectors.toList());
    }


    public ProcedureRequestedDTO updateProcedureRequestedById(Long id, ProcedureRequestedUpdateRequest request) {
        Procedure procedure = Procedure.findById(request.procedureId);

        ProcedureRequested procedureReq = ProcedureRequested.findById(id);

        if ("closed".equals(procedureReq.visit.visitStatus)) {
            throw new WebApplicationException("Your new password must be unique",409);

        }
        return proceduresRequestedRepository.findByIdOptional(id)
                .map(procedureRequested -> {

                    procedureRequested.doneBy = request.doneBy;
                    procedureRequested.orderedBy = request.orderedBy;
                    procedureRequested.report = request.report;
                    procedureRequested.quantity = request.quantity;
                    procedureRequested.unitSellingPrice = procedure.unitSellingPrice;
                    procedureRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(procedure.unitSellingPrice);
                    procedureRequested.procedureRequestedType = procedure.procedureType;
                    procedureRequested.updateDate = LocalDate.now();

                    proceduresRequestedRepository.persist(procedureRequested);

                    return new ProcedureRequestedDTO(procedureRequested);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }


    public List<ProcedureRequestedDTO> getLabTestProceduresByVisit(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is "LabTest" and visit ID matches, ordered descending
        List<ProcedureRequested> labTestProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC", // Replace 'id' with your desired field for sorting
                "LabTest",
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return labTestProcedures.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }






    public List<ProcedureRequestedDTO> getAllUltrasoundScanProcedures() {
        // Query for ProcedureRequested where procedureRequestedType is "LabTest" and visit ID matches, ordered descending
        List<ProcedureRequested> UltrasoundScan = ProcedureRequested.find(
                "category = ?1 ORDER BY id DESC", // Replace 'id' with your desired field for sorting
                "imaging"

        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return UltrasoundScan.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }







    public List<ProcedureRequestedDTO> getUltrasoundScanProceduresByVisit(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is "LabTest" and visit ID matches, ordered descending
        List<ProcedureRequested> UltrasoundScan = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC", // Replace 'id' with your desired field for sorting
                "imaging",
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return UltrasoundScan.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }


    public List<ProcedureRequestedDTO> getNonLabTestNonUltrasoundProceduresByVisit(Long visitId) {
        // Query for ProcedureRequested where procedureRequestedType is neither "LabTest" nor "Ultrasound" and visit ID matches, ordered descending
        List<ProcedureRequested> procedures = ProcedureRequested.find(
                "category NOT IN (?1, ?2, ?3) and visit.id = ?4 ORDER BY id DESC",
                "LabTest",
                "imaging",
                "consultation",
                visitId
        ).list();

        // Convert the results to a list of ProcedureRequestedDTO
        return procedures.stream()
                .map(ProcedureRequestedDTO::new)
                .toList();
    }


    public BigDecimal getLabTestProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> labTestProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "LabTest",
                visitId
        ).list();

        BigDecimal totalAmountSum;
        totalAmountSum = labTestProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return totalAmountSum;
    }


    public BigDecimal getScanProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "imaging",
                visitId
        ).list();

        BigDecimal totalAmountSum;
        totalAmountSum = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return totalAmountSum;
    }

  /*  public BigDecimal getAllScanProcedures() {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "category = ?1 ORDER BY id DESC",
                "imaging"

        ).list();

        BigDecimal totalAmountSum;
        totalAmountSum = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return totalAmountSum;
    }*/



    public List<BigDecimal> getTotalCostOfProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "Ultrasound",
                visitId
        ).list();

        List<ProcedureRequested> labTestsProcedures = ProcedureRequested.find(
                "category = ?1 and visit.id = ?2 ORDER BY id DESC",
                "LabTest",
                visitId
        ).list();

        BigDecimal ultrasoundTotalAmount = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal labTotalAmount = labTestsProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return List.of(labTotalAmount, ultrasoundTotalAmount);
    }



    @Transactional
    public Response deleteProcedureRequestById(Long id) {

        ProcedureRequested procedureRequested = proceduresRequestedRepository.findById(id);

        if (procedureRequested == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        if ("closed".equals(procedureRequested.visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Visit is closed. You cannot add anything. Please Open a new visit or contact Admin on 0784411848: ", null))
                    .build();
        }



        if(Objects.equals(procedureRequested.category, "imaging")){
            GeneralUs.delete("procedureRequested.id", id);

        }


        Invoice invoice = Invoice.find("visit.id", procedureRequested.visit.id).firstResult();

        InvoiceUpdateRequest request = new InvoiceUpdateRequest();
        request.tax = BigDecimal.ZERO;
        request.discount = BigDecimal.ZERO;

        invoiceService.updateInvoice(invoice.id, request);

        proceduresRequestedRepository.delete(procedureRequested);

        itemUsedService.restoreStockOnProcedureDelete(procedureRequested.procedureId);

        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }



    // final and working//











}
