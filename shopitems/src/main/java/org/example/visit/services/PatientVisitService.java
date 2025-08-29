package org.example.visit.services;

import com.itextpdf.io.IOException;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.PatientGroup;
import org.example.client.services.PatientGroupService;
import org.example.client.services.PatientService;
import org.example.client.services.payloads.responses.dtos.PatientDTO;
import org.example.client.services.payloads.responses.dtos.PatientGroupDTO;
import org.example.configuration.handler.ResponseMessage;
import org.example.client.domains.Patient;
import org.example.client.domains.repositories.PatientRepository;
import org.example.consultations.domains.ConsultationRepository;
import org.example.consultations.services.ConsultationService;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.domains.repositories.InvoiceRepository;
import org.example.finance.invoice.services.FooterHelperInvoice;
import org.example.finance.invoice.services.InvoiceService;
import org.example.visit.domains.PatientVisit;
import org.example.visit.domains.repositories.PatientVisitRepository;
import org.example.visit.services.paloads.requests.PatientVisitRequest;
import org.example.visit.services.paloads.requests.PatientVisitStatusUpdateRequest;
import org.example.visit.services.paloads.requests.PatientVisitUpdateRequest;
import org.example.visit.services.paloads.requests.VisitParametersRequest;
import org.example.visit.services.paloads.responses.FullVisitResponse;
import org.example.visit.services.paloads.responses.PatientVisitDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;



import com.itextpdf.io.IOException;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import com.itextpdf.layout.element.Image;

import java.io.InputStream;

import java.util.*;








import static io.quarkus.arc.ComponentsProvider.LOG;

@ApplicationScoped
public class PatientVisitService {

    @Inject
    PatientVisitRepository patientVisitRepository;

    @Inject
    PatientRepository patientRepository;

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InvoiceService invoiceService;

    @Inject
    ConsultationService consultationService;

    @Inject
    PatientGroupService patientGroupService;

    @Inject
    ConsultationRepository consultationRepository;

    @Inject
    MySQLPool client;

    public static final String NOT_FOUND = "Not found!";

    public Long patientRequestedId;

    /**
     * Creates a new PatientVisit for the specified patient ID.
     */
    @Transactional
    public PatientVisitDTO createNewPatientVisit(Long id, PatientVisitRequest request) {
        // Fetch the patient
        Patient patient = Patient.findById(id);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found for ID: " + id);
        }

        List<PatientVisit> openVisits = PatientVisit.find("patient.id = ?1 and visitStatus = ?2", id, "open").list();
        if (!openVisits.isEmpty()) {
            throw new IllegalStateException("Please first Close the open visits before opening a new one.");
        }

        // Generate unique visit number
        int lastVisitNumber = generateVisitNo(id);
        int newVisitNumber = lastVisitNumber + 1;

        // Ensure visitNumber is unique
        boolean exists = patientVisitRepository.find("patient.id = ?1 and visitNumber = ?2", id, newVisitNumber)
                .firstResultOptional()
                .isPresent();
        if (exists) {
            throw new IllegalArgumentException("Duplicate visitNumber: " + newVisitNumber + " for patient ID " + id);
        }

        // Create a new visit
        PatientVisit patientVisit = new PatientVisit();
        patientVisit.visitDate = LocalDate.now();
        patientVisit.visitTime = LocalTime.now();
        patientVisit.visitReason = "Consultation";

        patientVisit.patientName = patient.patientFirstName +" "+patient.patientSecondName;



        // PROPER NULL CHECKING - fix the error
        if (patient.patientGroup != null && patient.patientGroup.id != null) {
            PatientGroupDTO group = patientGroupService.getPatientGroupById(patient.patientGroup.id);
            patientVisit.visitGroup = group != null ? group.groupNameShortForm : null;
        } else {
            patientVisit.visitGroup = null;
        }

        patientVisit.visitStatus = "open";
        patientVisit.visitType = request.visitType;
        patientVisit.visitNumber = newVisitNumber;
        patientVisit.visitName = "Visit 0" + newVisitNumber;
        patientVisit.patient = patient;
        patientVisit.balanceDue = BigDecimal.valueOf(0.00);
        patientVisit.amountPaid = BigDecimal.valueOf(0.00);
        patientVisit.totalAmount = BigDecimal.valueOf(0.00);
        patientVisit.subTotal = BigDecimal.valueOf(0.00);

        // Persist visit
        patientVisitRepository.persist(patientVisit);

        invoiceService.createInvoice(patientVisit.id);
        consultationService.newConsultationOnTheGo(patientVisit.id);

        return new PatientVisitDTO(patientVisit);
    }





    @Transactional
    public void updateAllVisitGroupsAndFinancialsFromPatients() {
        // Fetch all patient visits
        List<PatientVisit> patientVisits = patientVisitRepository.listAll();

        // Update each visit with patient group name and financial data
        patientVisits.forEach(visit -> {
            // Update patient group and name
            if (visit.patient != null) {
                // Update patient group if available
                if (visit.patient.getPatientGroup() != null &&
                        visit.patient.getPatientGroup().getId() != null) {

                    PatientGroupDTO group = patientGroupService.getPatientGroupById(visit.patient.getPatientGroup().getId());
                    visit.visitGroup = group != null ? group.groupNameShortForm : null;
                } else {
                    visit.visitGroup = null;
                }

                // Update patient name - safely get the patient from database
                Patient patient = Patient.findById(visit.patient.id);
                if (patient != null) {
                    visit.patientName = patient.patientFirstName + " " + patient.patientSecondName;
                } else {
                    visit.patientName = "Unknown Patient";
                }
            } else {
                visit.visitGroup = null;
                visit.patientName = "Unknown Patient";
            }

            // Update financial fields from invoice
            updateFinancialFieldsFromInvoice(visit);
        });

        // Persist all updates
        patientVisitRepository.persist(patientVisits);
    }


    private void updateFinancialFieldsFromInvoice(PatientVisit visit) {
        // Get the invoice for this visit
        List<Invoice> invoices = invoiceRepository.find("visit.id = ?1", visit.id).list();

        if (invoices != null && !invoices.isEmpty()) {
            // Assuming one invoice per visit, get the first one
            Invoice invoice = invoices.get(0);

            // Update financial fields from invoice
            visit.balanceDue = invoice.getBalanceDue() != null ? invoice.getBalanceDue() : BigDecimal.ZERO;
            visit.amountPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
            visit.totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            visit.subTotal = invoice.getSubTotal() != null ? invoice.getSubTotal() : BigDecimal.ZERO;
        } else {
            // No invoice found, set default values
            visit.balanceDue = BigDecimal.ZERO;
            visit.amountPaid = BigDecimal.ZERO;
            visit.totalAmount = BigDecimal.ZERO;
            visit.subTotal = BigDecimal.ZERO;
        }
    }

    

    /**
     * Generates the next visit number for a given patient by fetching the highest existing visit number.
     */
    @Transactional
    public int generateVisitNo(Long patientId) {
        return patientVisitRepository.find("patient.id = ?1", Sort.descending("visitNumber"), patientId)
                .list()  // Sort by visit number in descending order
                .stream()
                .map(patientVisit -> patientVisit.visitNumber)  // Map to visit number
                .findFirst()  // Get the highest visit number, if exists
                .orElse(0);  // Return 0 if no visit found
    }
    @Transactional
    public int generateVisitNos(Long patientId) {
        // Query the highest visit number for the given patient
        Integer highestVisitNumber = patientVisitRepository.find("patient.id = ?1", Sort.descending("visitNumber"), patientId)
                .project(Integer.class) // Fetch the `visitNumber` as Integer
                .firstResult(); // Get the first result (highest visit number)

        // If no visits exist, default to 0; otherwise, add 1
        return (highestVisitNumber != null ? highestVisitNumber : 0) + 1;
    }

    @Transactional
    public List<PatientVisitDTO> getAllPatientVisits() {
        // Retrieve all PatientVisit records from the repository
        List<PatientVisit> patientVisits = patientVisitRepository.listAll();

        // Map each PatientVisit to a PatientVisitDTO
        return patientVisits.stream()
                .map(PatientVisitDTO::new)  // Mapping each entity to its DTO
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PatientVisitDTO> getAllPatients() {
        return patientVisitRepository.listAll(Sort.ascending("visitNumber"))
                .stream()
                .map(PatientVisitDTO::new)
                .toList();
    }

    @Transactional
    public List<PatientVisitDTO> getVisitByPatientId(Long patientId) {
        // Fetch the list of initial triage vitals by visitId
        List<PatientVisitDTO> result = patientVisitRepository
                .find("patient.id", patientId)
                .list()  // Fetch the result as a List
                .stream()  // Convert the result into a stream for further transformation
                .map(PatientVisitDTO::new)  // Map each entity to a DTO
                .toList();  // Collect the mapped entities into a list

        if (result.isEmpty()) {
            // If no results found, log the error and throw a 404 exception
            String errorMessage = String.format("No patient visits found for patientId: %d", patientId);
            LOG.error(errorMessage);
            throw new WebApplicationException(errorMessage, Response.Status.NOT_FOUND);
        }

        // Return the list of DTOs
        return result;
    }

    @Transactional
    public Response getLatestVisitByPatientId(Long patientId) {
        PatientVisit latestVisit = patientVisitRepository
                .find("patient.id", Sort.descending("id"), patientId)
                .firstResult();

        PatientVisitDTO latestVisitDTO;

        if (latestVisit == null) {
            PatientVisitRequest defaultRequest = new PatientVisitRequest();
            defaultRequest.visitType = "General Consultation";


            // Create a new visit
            latestVisitDTO = createNewPatientVisit(patientId, defaultRequest);
        } else {
            // Convert the found PatientVisit into DTO
            latestVisitDTO = new PatientVisitDTO(latestVisit);
        }

        return Response.ok(new ResponseMessage("Patient visit fetched successfully", latestVisitDTO)).build();
    }




    @Transactional
    public PatientVisitDTO updatePatientVisitById(Long id, PatientVisitUpdateRequest request) {
        return patientVisitRepository.findByIdOptional(id)
                .map(patientVisit -> {

                    patientVisit.visitType = request.visitType;
                    patientVisit.visitReason = request.visitReason;
                    patientVisit.visitLastUpdatedDate = LocalDate.now();

                    patientVisitRepository.persist(patientVisit);

                    return new PatientVisitDTO(patientVisit);
                }).orElseThrow(() -> new WebApplicationException(NOT_FOUND,404));
    }
    @Transactional
    public PatientVisitDTO updatePatientVisitStatusById(Long id, PatientVisitStatusUpdateRequest request) {
        return patientVisitRepository.findByIdOptional(id)
                .map(patientVisit -> {

                    // If the status to be updated is "open", and userRole is not "md", deny the operation
                    if ("open".equalsIgnoreCase(request.visitStatus) &&
                            (request.userRole == null || !request.userRole.equalsIgnoreCase("md"))) {

                        throw new WebApplicationException(
                                Response.status(Response.Status.BAD_REQUEST)
                                        .entity(new ResponseMessage("You need admin approval to open a visit", null))
                                        .build()
                        );
                    }

                    patientVisit.visitStatus = request.visitStatus;
                    patientVisit.visitLastUpdatedDate = LocalDate.now();

                    patientVisitRepository.persist(patientVisit);

                    return new PatientVisitDTO(patientVisit);
                })
                .orElseThrow(() -> new WebApplicationException("Visit not found", 404));
    }













    public List<PatientVisit> getVisitsAdvancedFilter(VisitParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
    SELECT
        id,
        patient_id,
        visitName,
        visitNumber,
        visitReason,
        visitType,
        subTotal,
        patientName,
        totalAmount,
        amountPaid,
        balanceDue,
        visitGroup,
        visitStatus,
        visitDate,
        visitLastUpdatedDate,
        visitTime
    FROM vena.patientvisit
    %s
    ORDER BY visitDate DESC;
    """.formatted(whereClause);

        return client.query(sql)
                .execute()
                .onItem()
                .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem()
                .transform(this::mapRowToPatientVisit)  // Map directly to PatientVisit
                .collect().asList()
                .await()
                .indefinitely();
    }

    private PatientVisit mapRowToPatientVisit(Row row) {

        PatientVisit visit = new PatientVisit();
        visit.id = row.getLong("id");
        // Create patient object first
        Patient patient = new Patient();
        patient.id = row.getLong("patient_id");
        visit.patient = patient;

        //visit.patient.id = row.getLong("patient_id");


        visit.visitName = row.getString("visitName");
        visit.patientName = row.getString("patientName");
        visit.visitNumber = row.getInteger("visitNumber");
        visit.visitReason = row.getString("visitReason");
        visit.visitType = row.getString("visitType");
        visit.visitStatus = row.getString("visitStatus");
        visit.visitDate = row.getLocalDate("visitDate");
        visit.visitLastUpdatedDate = row.getLocalDate("visitLastUpdatedDate");
        visit.visitTime = row.getLocalTime("visitTime");
        visit.subTotal = row.getBigDecimal("subTotal");
        visit.totalAmount = row.getBigDecimal("totalAmount");

        visit.amountPaid = row.getBigDecimal("amountPaid");
        visit.balanceDue = row.getBigDecimal("balanceDue");
        visit.visitGroup = row.getString("visitGroup");

        return visit;
    }


    private FullVisitResponse from(Row row){

        FullVisitResponse response = new FullVisitResponse();
        response.id = row.getLong("id");
        response.patientId = row.getLong("patient_id");
        response.visitName = row.getString("visitName");
        response.visitNumber = row.getInteger("visitNumber");
        response.visitReason = row.getString("visitReason");
        response.visitType = row.getString("visitType");
        response.visitStatus = row.getString("visitStatus");
        response.visitDate = row.getLocalDate("visitDate");
        response.visitLastUpdatedDate = row.getLocalDate("visitLastUpdatedDate");
        response.visitTime = row.getLocalTime("visitTime");
        response.subTotal = row.getBigDecimal("subTotal");
        response.totalAmount = row.getBigDecimal("totalAmount");

        response.amountPaid = row.getBigDecimal("amountPaid");
        response.balanceDue = row.getBigDecimal("balanceDue");
        response.visitGroup = row.getString("visitGroup");



        return response;
    }

    private StringJoiner getStringJoiner(VisitParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.visitGroup != null && !request.visitGroup.isEmpty()) {
            conditions.add("visitGroup = '" + request.visitGroup + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.datefrom != null && request.dateto != null) {
            conditions.add("visitDate BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }



    @Transactional
    public PatientVisitDebtResult calculateFinancialTotals(List<PatientVisit> filteredVisits) {

        // Calculate only the requested totals
        BigDecimal totalBalanceDue = filteredVisits.stream()
                .map(PatientVisit::getBalanceDue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmountPaid = filteredVisits.stream()
                .map(PatientVisit::getAmountPaid)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = filteredVisits.stream()
                .map(PatientVisit::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);



        // Convert to DTOs
        List<PatientVisitDTO> patientVisitDTOs = filteredVisits.stream()
                .map(PatientVisitDTO::new)
                .collect(Collectors.toList());

        return new PatientVisitDebtResult(
                patientVisitDTOs,
                totalBalanceDue,
                totalAmountPaid,
                totalAmount
                  // Add patient names to the result
        );
    }

    // Updated wrapper record with patient names
    public record PatientVisitDebtResult(
            List<PatientVisitDTO> patientVisits,
            BigDecimal totalBalanceDue,
            BigDecimal totalAmountPaid,
            BigDecimal totalAmount
    ) {}

    @Transactional
    public Response generateAndReturnInvoicePdfForListOfCompassionPatients(VisitParametersRequest request) {
        try {
            List<PatientVisit> filteredVisits = getVisitsAdvancedFilter(request);

            calculateFinancialTotals(filteredVisits);

            PatientVisitDebtResult result = calculateFinancialTotals(filteredVisits);

// Extract the data from the result
            List<PatientVisitDTO> patientVisitDTOs = result.patientVisits();
            BigDecimal totalBalanceDue = result.totalBalanceDue();
            BigDecimal totalAmountPaid = result.totalAmountPaid();
            BigDecimal totalAmount = result.totalAmount();

            String title = (totalAmountPaid != null && totalAmountPaid.compareTo(BigDecimal.ZERO) > 0)
                    ? "RECEIPT"
                    : "INVOICE";

            // Create the PDF document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHelperInvoice());

            Document document = new Document(pdfDocument);
            document.setMargins(36, 36, 90, 36);



            // Add invoice title
            Table invoiceTitle = new Table(new float[]{1});
            invoiceTitle.setWidthPercent(100);
            invoiceTitle.addCell(new Cell()
                    .add(new Div()
                            .setBorderBottom(new SolidBorder(1)) // Underline (1px solid line)
                            .setPaddingBottom(2) // Space between text and underline
                            .add(new Paragraph(title)
                                    .setBold()
                                    .setFontSize(11)
                                    .setTextAlignment(TextAlignment.CENTER)
                            )
                    )
                    .add(new Paragraph("Department of Finance - Veneranda Medical (A subsidiary of Veneranda Hospital)")
                            .setFontSize(7)
                            //.setItalic()
                            .setMarginTop(3)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(15)
            );
            document.add(invoiceTitle);

            // Add header: Logo and Invoice Details
            Table headerTable = new Table(new float[]{1, 1, 1, 2, 1});
            headerTable.setWidthPercent(100);

            // Add logo
            headerTable.addCell(new Cell()
                    .add(getLogo().setWidth(79).setHeight(68))
                    .setBorder(Border.NO_BORDER)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                    .setVerticalAlignment(VerticalAlignment.TOP)
                    .setPaddingTop(-7)
                    .setPaddingLeft(-22)
            );

            // Add invoice details
            headerTable.addCell(new Cell()
                    .add(new Paragraph("FROM: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("ADDRESS: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("TO: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("ADDRESS: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("VENERANDA MEDICAL").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("BUGOGO TOWN COUNCIL, KYEGEGWA DISTRICT").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("KATOMA CHILD AND YOUTH DEVELOPMENT CENTER")
                            .setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph()
                            .add(Optional.of("KATOMA - BUGOGO TOWN COUNCIL, KYEGEGWA DISTRICT")
                                    .map(String::toUpperCase)
                                    .orElse(""))
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT)
                    )


                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("NUMBER: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("DUE DATE RANGE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("DATE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("BALANCE DUE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("UG-5003").setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(request.datefrom +" - "+ request.dateto)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(LocalDate.now())).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf("UGX: "+totalBalanceDue)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))

                    .setBorder(Border.NO_BORDER)
            );

            document.add(headerTable);

            // Add items table
            float[] columnWidths = {4, 1, 2, 2, 2};
            Table itemsTable = new Table(columnWidths);
            itemsTable.setWidthPercent(100);

            // Add header with no column lines
            itemsTable.addCell(createCell("CLIENT NAME", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.color.Color.WHITE)
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("SERVICE", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.color.Color.WHITE)
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("AMOUNT TO PAY (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.color.Color.WHITE)
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("AMOUNT PAID (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.color.Color.WHITE)
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("BALANCE DUE (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.color.Color.WHITE)
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK)
                    .setBorder(Border.NO_BORDER));



            boolean isEvenRow = false;
// Calculate the totals



            for (PatientVisitDTO patientVisitDto : patientVisitDTOs) {
                com.itextpdf.kernel.color.Color rowColor = isEvenRow
                        ? com.itextpdf.kernel.color.Color.WHITE
                        : com.itextpdf.kernel.color.Color.LIGHT_GRAY;

                String patientName = patientVisitDto.patientName != null ? patientVisitDto.patientName.toUpperCase() : "UNKNOWN PATIENT";

                itemsTable.addCell(createCell(patientName, 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf("MEDICAL BILLS"), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(patientVisitDto.totalAmount), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(patientVisitDto.amountPaid), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(patientVisitDto.balanceDue), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                isEvenRow = !isEvenRow;
            }



            document.add(itemsTable);



            // Add totals table
            Table totalsTable = new Table(new float[]{4, 2, 2});
            totalsTable.setWidthPercent(100);

            //PatientGroupDTO patientDTO = patientGroupService.getPatientGroupById(1);


            Cell notesCell1 = new Cell(6, 1)
                    .add(("\n IMPRESSION / DIAGNOSIS: " ))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.TOP);
            totalsTable.addCell(notesCell1);

            //BigDecimal totalDebt = result.totalBalanceDue();

            // Add discount row
            totalsTable.addCell(createCell("DISCOUNT:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.LIGHT_GRAY));
            totalsTable.addCell(createCell("0", 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.LIGHT_GRAY));

            // Add tax row
            totalsTable.addCell(createCell("TAX:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell("0", 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            // Add total amount row
            totalsTable.addCell(createCell("TOTAL AMOUNT:", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.LIGHT_GRAY));
            totalsTable.addCell(createCell(String.valueOf(totalAmount), 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.LIGHT_GRAY));

            // Add amount paid row
            totalsTable.addCell(createCell("AMOUNT PAID:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell(String.valueOf(totalAmountPaid), 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            // Add balance due row
            totalsTable.addCell(createCell("BALANCE DUE:", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.LIGHT_GRAY));
            totalsTable.addCell(createCell(String.valueOf(totalBalanceDue), 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.LIGHT_GRAY));

            document.add(totalsTable);

            // Close the document
            document.close();

            // Return the PDF as a response
            byte[] pdfBytes = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=invoice.pdf")
                    .type("application/pdf")
                    .build();

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Utility method to create cells with alignment
    private Cell createCell(String content, int i, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setTextAlignment(alignment);
        return cell;
    }

    public static Image getLogo() {
        try {
            // Load the image as a stream from the classpath
            InputStream logoStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("logo.png");

            if (logoStream == null) {
                throw new RuntimeException("Logo image not found in classpath.");
            }

            byte[] imageBytes = logoStream.readAllBytes(); // Java 9+; use IOUtils for Java 8
            ImageData imageData = ImageDataFactory.create(imageBytes);

            Image logo = new Image(imageData);
            logo.scaleToFit(80, 80);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

            return logo;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the logo image.", e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }





































}
