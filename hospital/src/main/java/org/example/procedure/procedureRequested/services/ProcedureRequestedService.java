package org.example.procedure.procedureRequested.services;

import io.quarkus.hibernate.orm.panache.Panache;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.GeneralUs;
import org.example.diagnostics.ultrasoundScan.generalUs.services.GeneralUsService;
import org.example.finance.invoice.services.InvoiceService;
import org.example.lab.cbc.domains.Cbc;
import org.example.lab.generalReport.domains.GeneralLabReport;
import org.example.lab.generalReport.services.GeneralLabReportService;
import org.example.lab.parasitologyStool.domains.ParasitologyStool;
import org.example.lab.singleStatementReport.malaria.domains.Malaria;
import org.example.lab.singleStatementReport.malaria.services.MalariaService;
import org.example.lab.urinalysis.domains.Urinalysis;
import org.example.lab.urinalysis.services.UrinalysisService;
import org.example.messages.services.MessagingWebSocketRegistry;
import org.example.messages.services.payloads.responses.MessagePushEvent;
import org.example.procedure.itemUsedInProcedure.services.ItemUsedService;
import org.example.procedure.procedure.domains.Procedure;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.procedure.procedureRequested.services.payloads.requests.ProcedureRequestedRequest;
import org.example.procedure.procedureRequested.services.payloads.requests.ProcedureRequestedUpdateRequest;
import org.example.procedure.procedureRequested.services.payloads.responses.ProcedureRequestedDTO;
import org.example.subscription.services.SpecialPrivilegeService;
import org.example.visit.domains.PatientVisit;

@ApplicationScoped
public class ProcedureRequestedService {
    @Inject
    ProcedureRequestedRepository proceduresRequestedRepository;
    @Inject
    InvoiceService invoiceService;
    @Inject
    ItemUsedService itemUsedService;
    @Inject
    SpecialPrivilegeService specialPrivilegeService;
    @Inject
    GeneralUsService generalUsService;
    @Inject
    MalariaService malariaService;
    @Inject
    GeneralLabReportService generalLabReportService;
    @Inject
    UrinalysisService urinalysisService;
    @Inject
    MessagingWebSocketRegistry messagingWebSocketRegistry;
    public static final String NOT_FOUND = "Not found!";
    public static final String VISIT_CLOSED = "Not found!";

    @Transactional
    public Response createNewProcedureRequested(Long visitID, ProcedureRequestedRequest request) {
        boolean creditAllowed;
        boolean consultationAlreadyDone;
        PatientVisit patientVisit = (PatientVisit)PatientVisit.findById(visitID);
        if (patientVisit == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Patient visit not found for ID: " + visitID, null)).build();
        }
        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Visit is closed. You cannot add anything. Please Open a new visit or contact Admin on 0784411848: ", null)).build();
        }
        Procedure procedure = (Procedure)Procedure.findById(request.procedureId);
        if (procedure == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Invalid procedure ID: " + request.procedureId, null)).build();
        }
        if (procedure.category != null && "consultation".equalsIgnoreCase(procedure.category.name) && (consultationAlreadyDone = this.proceduresRequestedRepository.find("category = ?1 and visit.id = ?2", new Object[]{"consultation", visitID}).firstResultOptional().isPresent())) {
            return Response.status(Response.Status.CONFLICT).entity(new ResponseMessage("Consultation already done for this visit", null)).build();
        }
        BigDecimal totalBalanceDue = this.invoiceService.calculateTotalBalanceDueForClosedVisits(patientVisit.patient.id);
        boolean bl = creditAllowed = patientVisit.patient.patientGroup != null && this.specialPrivilegeService.groupAllowsCreditDespiteDebt(patientVisit.patient.patientGroup, null);
        if (totalBalanceDue.compareTo(BigDecimal.ZERO) > 0 && !creditAllowed) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Cannot access any service. Patient has a debt of: " + String.valueOf(totalBalanceDue) + " and doesn't belong to an authorized credit group. Please clear the debt first or contact Admin")).build();
        }
        if ("closed".equals(patientVisit.visitStatus)) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage("Visit is closed. You cannot add anything. Open a new visit or contact Admin on 0784411848: ", null)).build();
        }
        if (procedure.category.parent == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ResponseMessage(procedure.procedureName + " Service has no category, please give it a category and try again", null)).build();
        }
        ProcedureRequested procedureRequested = new ProcedureRequested();
        procedureRequested.procedure = procedure;
        procedureRequested.patientName = patientVisit.patient.patientFirstName + " " + patientVisit.patient.patientSecondName;
        procedureRequested.quantity = request.quantity;
        procedureRequested.report = "";
        procedureRequested.procedureId = request.procedureId;
        procedureRequested.orderedBy = request.orderedBy;
        procedureRequested.unitSellingPrice = request.unitSellingPrice;
        procedureRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(request.unitSellingPrice);
        procedureRequested.visit = patientVisit;
        procedureRequested.procedureRequestedName = procedure.procedureName;
        procedureRequested.exam = procedure.procedureName;
        procedureRequested.indication = request.indication;
        procedureRequested.status = "pending";
        procedureRequested.bgColor = "rgb(26, 139, 204)";
        procedureRequested.category = procedure.category != null && procedure.parentCategory != null ? procedure.parentCategory.name : null;
        procedureRequested.dateOfProcedure = LocalDate.now();
        procedureRequested.timeOfProcedure = LocalTime.now();
        this.proceduresRequestedRepository.persist(procedureRequested);
        try {
            this.messagingWebSocketRegistry.broadcast(MessagePushEvent.procedureRequested(procedureRequested));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return Response.ok(new ResponseMessage("New procedure request made successfully", new ProcedureRequestedDTO(procedureRequested))).build();
    }

    @Transactional
    public Response updateMissingProcedureReferences() {
        List<ProcedureRequested> procedureRequestedList = ProcedureRequested.find((String)"procedure is null and procedureId is not null", (Object[])new Object[0]).list();
        int updatedCount = 0;
        int notFoundCount = 0;
        for (ProcedureRequested procedureRequested : procedureRequestedList) {
            if (procedureRequested.procedureId == null) continue;
            Procedure procedure = (Procedure)Procedure.findById(procedureRequested.procedureId);
            if (procedure != null) {
                procedureRequested.procedure = procedure;
                this.proceduresRequestedRepository.persist(procedureRequested);
                ++updatedCount;
                continue;
            }
            ++notFoundCount;
        }
        String message = String.format("Update completed. Updated: %d records, Procedure not found for: %d records", updatedCount, notFoundCount);
        return Response.ok(new ResponseMessage(message, null)).build();
    }

    public ProcedureRequestedDTO getOtherRequestedProcedureById(Long id) {
        return this.proceduresRequestedRepository.findByIdOptional(id).map(ProcedureRequestedDTO::new).orElseThrow(() -> new WebApplicationException("ProcedureRequested not found", 404));
    }

    public List<ProcedureRequestedDTO> getRequestedProceduresByVisitId(Long visitId) {
        List<ProcedureRequested> requestedProcedures = this.proceduresRequestedRepository.find("visit.id = ?1 ORDER BY id DESC", new Object[]{visitId}).list();
        return requestedProcedures.stream().map(ProcedureRequestedDTO::new).collect(Collectors.toList());
    }

    public ProcedureRequestedDTO updateProcedureRequestedById(Long id, ProcedureRequestedUpdateRequest request) {
        Procedure procedure = (Procedure)Procedure.findById(request.procedureId);
        ProcedureRequested procedureReq = (ProcedureRequested)ProcedureRequested.findById(id);
        if ("closed".equals(procedureReq.visit.visitStatus)) {
            throw new WebApplicationException("Your new password must be unique", 409);
        }
        return this.proceduresRequestedRepository.findByIdOptional(id).map(procedureRequested -> {
            procedureRequested.doneBy = request.doneBy;
            procedureRequested.orderedBy = request.orderedBy;
            procedureRequested.report = request.report;
            procedureRequested.indication = request.indication;
            procedureRequested.quantity = request.quantity;
            procedureRequested.unitSellingPrice = procedure.unitSellingPrice;
            procedureRequested.totalAmount = BigDecimal.valueOf(request.quantity).multiply(procedure.unitSellingPrice);
            procedureRequested.procedureRequestedName = procedure.procedureName;
            procedureRequested.updateDate = LocalDate.now();
            this.proceduresRequestedRepository.persist(procedureRequested);
            return new ProcedureRequestedDTO(procedureRequested);
        }).orElseThrow(() -> new WebApplicationException("Not found!", 404));
    }

    public List<ProcedureRequestedDTO> getLabTestProceduresByVisit(Long visitId) {
        List<ProcedureRequested> labTestProcedures = ProcedureRequested.find((String)"SELECT DISTINCT p FROM ProcedureRequested p LEFT JOIN FETCH p.procedure LEFT JOIN FETCH p.procedure.category WHERE p.category = ?1 AND p.visit.id = ?2 ORDER BY p.id DESC", (Object[])new Object[]{"labtest", visitId}).list();
        return labTestProcedures.stream().map(ProcedureRequestedDTO::new).toList();
    }

    public List<ProcedureRequestedDTO> getAllLabTestProcedures() {
        List<ProcedureRequested> labTests = ProcedureRequested.find((String)"    SELECT DISTINCT p\n    FROM ProcedureRequested p\n    JOIN FETCH p.procedure pr\n    JOIN FETCH pr.parentCategory pc\n    WHERE LOWER(pc.name) = LOWER(?1)\n    ORDER BY p.id DESC\n", (Object[])new Object[]{"labtest"}).list();
        return labTests.stream().map(ProcedureRequestedDTO::new).toList();
    }

    public List<ProcedureRequestedDTO> getAllUltrasoundScanProcedures() {
        List<ProcedureRequested> ultrasoundScans = ProcedureRequested.find((String)"    SELECT DISTINCT p\n    FROM ProcedureRequested p\n    JOIN FETCH p.procedure pr\n    JOIN FETCH pr.category c\n    WHERE LOWER(c.name) = LOWER(?1)\n    ORDER BY p.id DESC\n", (Object[])new Object[]{"ultrasound scan"}).list();
        return ultrasoundScans.stream().map(ProcedureRequestedDTO::new).toList();
    }

    public List<ProcedureRequestedDTO> getAllDentalProcedures() {
        List<ProcedureRequested> dentalProcedures = ProcedureRequested.find((String)"SELECT DISTINCT p FROM ProcedureRequested p LEFT JOIN FETCH p.procedure pr LEFT JOIN FETCH pr.parentCategory pc WHERE pc.name = ?1 ORDER BY p.id DESC", (Object[])new Object[]{"dental"}).list();
        return dentalProcedures.stream().map(ProcedureRequestedDTO::new).toList();
    }

    public List<ProcedureRequestedDTO> getUltrasoundScanProceduresByVisit(Long visitId) {
        List<ProcedureRequested> UltrasoundScan = ProcedureRequested.find((String)"SELECT DISTINCT p FROM ProcedureRequested p LEFT JOIN FETCH p.procedure LEFT JOIN FETCH p.procedure.category WHERE p.category = ?1 AND p.visit.id = ?2 ORDER BY p.id DESC", (Object[])new Object[]{"imaging", visitId}).list();
        return UltrasoundScan.stream().map(ProcedureRequestedDTO::new).toList();
    }

    public List<ProcedureRequestedDTO> getNonLabTestNonUltrasoundProceduresByVisit(Long visitId) {
        List<ProcedureRequested> procedures = ProcedureRequested.find((String)"SELECT p\nFROM ProcedureRequested p\nJOIN p.procedure pr\nLEFT JOIN pr.parentCategory pc\nLEFT JOIN pr.category c\nWHERE p.visit.id = ?1\nAND (\n        (pc IS NULL OR LOWER(pc.name) <> LOWER(?2))\n    AND (c IS NULL OR LOWER(c.name) NOT IN (?3, ?4))\n)\nORDER BY p.id DESC\n", (Object[])new Object[]{visitId, "labtest", "imaging", "consultation"}).list();
        return procedures.stream().map(ProcedureRequestedDTO::new).toList();
    }

    public BigDecimal getLabTestProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> labTestProcedures = ProcedureRequested.find((String)"SELECT p\nFROM ProcedureRequested p\nJOIN p.procedure pr\nJOIN pr.parentCategory pc\nWHERE LOWER(pc.name) = LOWER(?1)\nAND p.visit.id = ?2\nORDER BY p.id DESC\n", (Object[])new Object[]{"labtest", visitId}).list();
        return labTestProcedures.stream().map(p -> p.totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getScanProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find((String)"category = ?1 and visit.id = ?2 ORDER BY id DESC", (Object[])new Object[]{"ultrasound scan", visitId}).list();
        BigDecimal totalAmountSum = scanProcedures.stream().map(procedureRequested -> procedureRequested.totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalAmountSum;
    }

    public List<BigDecimal> getTotalCostOfProceduresAndSumByVisit(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find((String)"category = ?1 and visit.id = ?2 ORDER BY id DESC", (Object[])new Object[]{"Ultrasound", visitId}).list();
        List<ProcedureRequested> labTestsProcedures = ProcedureRequested.find((String)"category = ?1 and visit.id = ?2 ORDER BY id DESC", (Object[])new Object[]{"labtest", visitId}).list();
        BigDecimal ultrasoundTotalAmount = scanProcedures.stream().map(procedureRequested -> procedureRequested.totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal labTotalAmount = labTestsProcedures.stream().map(procedureRequested -> procedureRequested.totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return List.of(labTotalAmount, ultrasoundTotalAmount);
    }

    @Transactional
    public Response deleteProcedureRequestById(Long id) {
        ProcedureRequested procedureRequested = (ProcedureRequested)(this.proceduresRequestedRepository.findById(id));
        if (procedureRequested == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        PatientVisit visit = procedureRequested.visit;
        if (visit != null && "closed".equals(visit.visitStatus)) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Visit is closed. You cannot add anything. Please Open a new visit or contact Admin on 0784411848: ", null)).build();
        }
        GeneralUs generalUs = (GeneralUs)(GeneralUs.find((String)"procedureRequested.id = ?1", (Object[])new Object[]{id}).firstResult());
        Malaria malaria = (Malaria)(Malaria.find((String)"procedureRequested.id = ?1", (Object[])new Object[]{id}).firstResult());
        if (Objects.equals(procedureRequested.category, "imaging") && generalUs != null && (generalUs.findings != null && !generalUs.findings.isEmpty() || generalUs.impression != null && !generalUs.impression.isEmpty() || generalUs.indication != null && !generalUs.indication.isEmpty())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Cannot delete scan report with findings ", null)).build();
        }
        if (Objects.equals(procedureRequested.procedureRequestedType, "malariatest") && malaria != null && (malaria.bs != null && !malaria.bs.trim().isEmpty() || malaria.mrdt != null && !malaria.mrdt.trim().isEmpty())) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ResponseMessage("Cannot delete MALARIA report with bs or mrdt results ", null)).build();
        }
        Long visitId = visit != null ? visit.id : null;
        Long catalogProcedureId = procedureRequested.procedureId;
        GeneralUs.delete((String)"procedureRequested.id", (Object[])new Object[]{id});
        Malaria.delete((String)"procedureRequested.id", (Object[])new Object[]{id});
        GeneralLabReport.delete((String)"procedureRequested.id", (Object[])new Object[]{id});
        Urinalysis.delete((String)"procedureRequested.id", (Object[])new Object[]{id});
        Cbc.delete((String)"procedureRequested.id", (Object[])new Object[]{id});
        ParasitologyStool.delete((String)"procedureRequested.id", (Object[])new Object[]{id});
        Panache.getEntityManager().flush();
        if (visit != null && visit.getProceduresRequested() != null) {
            visit.getProceduresRequested().removeIf(p -> p != null && Objects.equals(p.id, id));
        }
        this.proceduresRequestedRepository.delete(procedureRequested);
        Panache.getEntityManager().flush();
        if (catalogProcedureId != null) {
            this.itemUsedService.restoreStockOnProcedureDelete(catalogProcedureId);
        }
        if (visitId != null) {
            this.invoiceService.syncInvoiceTotalsForVisit(visitId);
        }
        return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
    }
}
