package org.example.services;

import com.itextpdf.io.IOException;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
//0782166412 surgical clinic level // 0788636441
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.*;


import java.awt.*;

import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.HorizontalAlignment;


import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.configuration.handler.ActionMessages;
import org.example.configuration.handler.ResponseMessage;
import org.example.domains.*;
import org.example.domains.repositories.InvoiceRepository;
import org.example.domains.repositories.PatientVisitRepository;
import org.example.services.payloads.requests.InvoiceUpdateRequest;
import org.example.services.payloads.responses.dtos.InvoiceDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

@ApplicationScoped
public class InvoiceService {

    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    PatientVisitRepository patientVisitRepository;

    @Inject
    PaymentService paymentService;

    private static final String NOT_FOUND = "Not found!";

    @Transactional
    public Invoice createInvoice(Long visitId) {
        // Check if the visit already has an invoice
        List<Invoice> existingInvoice = Invoice.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

        // Check if the list is NOT empty
        if (!existingInvoice.isEmpty()) {
            throw new IllegalArgumentException("Invoice already exists for this visit.");
        }

        // Find the patient visit
        PatientVisit patientVisit = PatientVisit.findById(visitId);

        // Throw exception if the visit is not found
        if (patientVisit == null) {
            throw new IllegalArgumentException("Visit not found.");
        }

        // Create the invoice
        Invoice invoice = new Invoice();
        invoice.visit = patientVisit;
        invoice.patient = patientVisit.patient;
        invoice.tin = "185 7564 3489";
        invoice.notes = "Type in a brief note";
        invoice.discount = BigDecimal.valueOf(0.00);
        invoice.tax = BigDecimal.valueOf(0.00);
        invoice.dateOfInvoice = LocalDate.now();
        invoice.timeOfCreation = LocalTime.now();
        invoice.toName = patientVisit.patient.patientFirstName + " " + patientVisit.patient.patientSecondName;
        invoice.fromName = "VENERANDA MEDICAL";
        invoice.fromAddress = "Bugogo Town Council-Kyegegwa District";
        invoice.toAddress = "Bugogo Town Council-Kyegegwa District";
        invoice.companyLogo = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2FAsset%201.png?alt=media&token=08b34d6a-0693-4dff-88b1-6e42b5c56f67";
        invoice.documentTitle = "INVOICE";
        invoice.invoicePlainNo = findMaxInvoiceNoReturnInt() + 1;
        invoice.invoiceNo = "VMDINV-" + invoice.invoicePlainNo;
        invoice.reference = generateRandomReferenceNo(20);
        invoice.subTotal = BigDecimal.valueOf(0.00);
        invoice.totalAmount = BigDecimal.valueOf(0.00);
        invoice.balanceDue = BigDecimal.valueOf(0.00);
        invoice.amountPaid = BigDecimal.valueOf(0.00);

        // Persist the invoice
        invoiceRepository.persist(invoice);

        //updateSubTotal(subTotalCalculated, visitId);

        return invoice;
    }


    @Transactional
    public Response updateInvoice(Long invoiceId, InvoiceUpdateRequest request) {
        // Find the existing invoice
        Invoice invoice = Invoice.findById(invoiceId);

        if (invoice == null) {
            //throw new IllegalArgumentException("Invoice not found.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Invoice not found..",null))
                    .build();
        }

        if (request.discount.compareTo(BigDecimal.ZERO) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Discount must be greater than zero.",null))
                    .build();
            //throw new IllegalArgumentException("Amount to pay must be greater than zero.");
        }

        if (request.tax.compareTo(BigDecimal.ZERO) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("Tax must be greater than zero.",null))
                    .build();
            //throw new IllegalArgumentException("Amount to pay must be greater than zero.");
        }


        // Recalculate subtotal, total amount, and balance due
        Map<String, BigDecimal> invoiceSubTotalMap = getInvoiceSubTotal(invoice.visit.id);
        BigDecimal subTotalCalculated = invoiceSubTotalMap.get("InvoiceSubtotal");
        invoice.subTotal = subTotalCalculated;

        if (subTotalCalculated == null || subTotalCalculated.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("SubTotal Amount must be greater than zero.",null))
                    .build();
            //throw new IllegalArgumentException("Amount to pay must be greater than zero.");
        }

// Ensure subTotalCalculated is greater than the discount
        if (subTotalCalculated.compareTo(request.discount) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseMessage("SubTotal Amount must be greater than the discount.", null))
                    .build();
        }

        BigDecimal totalAmountDiscounted = subTotalCalculated.subtract(request.discount);
        BigDecimal totalAmount = totalAmountDiscounted.add(request.tax);
        invoice.totalAmount = totalAmount;

        invoice.amountPaid = paymentService.getTotalPaymentOfInvoice(invoiceId);
        invoice.balanceDue = totalAmount.subtract(paymentService.getTotalPaymentOfInvoice(invoiceId));

        // Update the new fields
        invoice.upDateOfInvoice = LocalDate.now();
        invoice.updateTimeOfCreation = LocalTime.now();

        // Update optional fields
        invoice.notes = request.notes;

        // Set discount: if request.discount is null, assign it to 0.00
        invoice.discount = (request.discount != null) ? request.discount : BigDecimal.valueOf(0.00);

// Set tax: if request.tax is null, assign it to 0.00
        invoice.tax = (request.tax != null) ? request.tax : BigDecimal.valueOf(0.00);

        // Persist the updated invoice
        invoiceRepository.persist(invoice);

       // return new InvoiceDTO(invoice);

        return Response.ok(new ResponseMessage("Invoice updated successfully", new InvoiceDTO(invoice))).build();

    }

    @Transactional
    public void updateInvoiceAmountPaid(Invoice invoice) {
        // Calculate the total payments made for the invoice
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found.");
        }
        BigDecimal totalPayments = paymentService.getTotalPaymentOfInvoice(invoice.id);

        totalPayments = totalPayments != null ? totalPayments : BigDecimal.ZERO;

        // Update the invoice fields
        invoice.amountPaid = totalPayments;
        invoice.balanceDue = invoice.totalAmount.subtract(totalPayments);

        // Persist the updated invoice
        invoiceRepository.persist(invoice);
    }

    @Transactional
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.listAll(Sort.descending("invoicePlainNo"))
                .stream()
                .map(InvoiceDTO::new)
                .toList();
    }

    @Transactional
    public int findMaxInvoiceNoReturnInt() {
        return invoiceRepository.listAll(Sort.descending("invoicePlainNo"))
                .stream()
                .map(invoice -> invoice.invoicePlainNo)
                .findFirst()
                .orElse(0);
    }

    @Transactional
    public String generateRandomReferenceNo(int length) {
        // Define characters that can be used in the password
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder reference = new StringBuilder();
        // Generate a random reference of the specified length
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            reference.append(characters.charAt(randomIndex));
        }

        return reference.toString();
    }

    @Transactional
    public BigDecimal checkIfConsultationWasDone(Long visitId) {
        // Check if initial vitals were taken
        boolean vitalsTaken = InitialTriageVitals.find("visit.id = ?1", visitId).firstResultOptional().isPresent();

        // Check if any treatments were given
        boolean treatmentsGiven = TreatmentRequested.find("visit.id = ?1", visitId).firstResultOptional().isPresent();

        // Check if any procedures were requested
        boolean proceduresDone = ProcedureRequested.find("visit.id = ?1", visitId).firstResultOptional().isPresent();

        boolean consultation = Consultation.find("visit.id = ?1", visitId).firstResultOptional().isPresent();

        // If any of the above conditions are true, return 10,000 shillings
        if (consultation) {
            return new BigDecimal("10000");
        }

        // Otherwise, return 0
        return BigDecimal.ZERO;
    }

    @Transactional
    public Map<String, BigDecimal> getInvoiceSubTotal(Long visitId) {
        List<ProcedureRequested> scanProcedures = ProcedureRequested.find(
                "procedureRequestedType = ?1 and visit.id = ?2 ORDER BY id DESC",
                "imaging",
                visitId
        ).list();

        List<ProcedureRequested> labTestsProcedures = ProcedureRequested.find(
                "procedureRequestedType = ?1 and visit.id = ?2 ORDER BY id DESC",
                "LabTest",
                visitId
        ).list();

        List<ProcedureRequested> otherProcedures = ProcedureRequested.find(
                "procedureRequestedType NOT IN (?1, ?2) and visit.id = ?3 ORDER BY id DESC",
                "LabTest",
                "imaging",
                visitId
        ).list();

        List<TreatmentRequested> treatmentGive = TreatmentRequested.find(
                "visit.id = ?1 ORDER BY id DESC",
                visitId
        ).list();

        BigDecimal consultationFee = checkIfConsultationWasDone(visitId);

        BigDecimal ultrasoundTotalAmount = scanProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal labTotalAmount = labTestsProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal otherProcedureTotalAmount = otherProcedures.stream()
                .map(procedureRequested -> procedureRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal treatmentTotalCost = treatmentGive.stream()
                .map(treatmentRequested -> treatmentRequested.totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal invoiceSubtotal = labTotalAmount
                .add(ultrasoundTotalAmount)
                .add(treatmentTotalCost)
                .add(otherProcedureTotalAmount)
                .add(consultationFee);


        PatientVisit visit = patientVisitRepository.findById(visitId);
        if (visit == null) {
            throw new IllegalArgumentException("Visit not found.");
        }

        // Check if the visit has an invoice
        if (visit.invoice == null || visit.invoice.isEmpty()) {
            // Create a new invoice
            Invoice newInvoice = new Invoice();
            newInvoice.visit = visit; // Associate the invoice with the visit
            newInvoice.patient = visit.patient;
            newInvoice.tin = "185 7564 3489";
            newInvoice.notes = "Type in a brief note";
            newInvoice.dateOfInvoice = LocalDate.now();
            newInvoice.timeOfCreation = LocalTime.now();
            newInvoice.toName = visit.patient.patientFirstName + " " + visit.patient.patientSecondName;
            newInvoice.fromName = "VENERANDA MEDICAL";
            newInvoice.fromAddress = "Bugogo Town Council-Kyegegwa District";
            newInvoice.toAddress = "Bugogo Town Council-Kyegegwa District";
            newInvoice.companyLogo = "https://firebasestorage.googleapis.com/v0/b/newstorageforuplodapp.appspot.com/o/images%2FAsset%201.png?alt=media&token=08b34d6a-0693-4dff-88b1-6e42b5c56f67";
            newInvoice.documentTitle = "INVOICE";
            newInvoice.invoicePlainNo = findMaxInvoiceNoReturnInt() + 1;
            newInvoice.invoiceNo = "VMDINV-" + findMaxInvoiceNoReturnInt() + 1;
            newInvoice.reference = generateRandomReferenceNo(20);
            newInvoice.subTotal = invoiceSubtotal; // Initialize subtotal
            newInvoice.discount = BigDecimal.ZERO; // Initialize discount
            newInvoice.tax = BigDecimal.ZERO; // Initialize tax
            newInvoice.totalAmount = invoiceSubtotal; // Initialize total amount
            BigDecimal totalAmountPaid = paymentService.getTotalPaymentOfVisit(visitId);
            newInvoice.amountPaid = totalAmountPaid; // Initialize amount paid
            newInvoice.balanceDue = invoiceSubtotal.subtract(totalAmountPaid); // Initialize balance due

            // Persist the new invoice
            invoiceRepository.persist(newInvoice);

            // Add the new invoice to the visit
            if (visit.invoice == null) {
                visit.invoice = new ArrayList<>();
            }
            visit.invoice.add(newInvoice);

            // Update the visit in the repository
            patientVisitRepository.persist(visit);
        }

        // Return the patient associated with the visit

        //updateSubTotal(invoiceSubtotal, visitId);

        // Get the first invoice from the list (or handle multiple invoices as needed)
        Invoice invoiceUpdate = visit.invoice.get(0); // Assuming visit.invoice is a List<Invoice>

        // Update the invoice fields
        invoiceUpdate.subTotal = invoiceSubtotal;
        //BigDecimal totalAmount = invoiceSubtotal.subtract(invoiceUpdate.discount.add(invoiceUpdate.tax));
        //BigDecimal totalAmount = (invoiceSubtotal.subtract(invoiceUpdate.discount)).subtract(invoiceUpdate.tax);
        BigDecimal totalAmountDiscounted = invoiceSubtotal.subtract(invoiceUpdate.discount);

        BigDecimal totalAmount = totalAmountDiscounted.add(invoiceUpdate.tax);


        invoiceUpdate.totalAmount = totalAmount;

        invoiceUpdate.amountPaid = paymentService.getTotalPaymentOfInvoice(invoiceUpdate.id);
        invoiceUpdate.balanceDue = totalAmount.subtract(paymentService.getTotalPaymentOfInvoice(invoiceUpdate.id));

        // Persist the updated invoice
        invoiceRepository.persist(invoiceUpdate);

        List<Invoice> allInvoices = Invoice.find(
                "patient.id = ?1 ORDER BY id DESC",
                visit.getPatient().getId()
        ).list();



        Invoice invoiceWithVisitId = invoiceRepository.find(
                "visit.id", visitId
        ).firstResult();

        if (invoiceWithVisitId == null) {
            throw new IllegalArgumentException("invoice not found.");

        }

        BigDecimal invoiceBalanceDue = invoiceWithVisitId.balanceDue;

        Long invoiceId = invoiceWithVisitId.id;


        BigDecimal totalAmountDue = allInvoices.stream()
                .map(invoice -> invoice.balanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);




        // Return as a map with keys for clarity
        Map<String, BigDecimal> totalCostMap = new HashMap<>();


        totalCostMap.put("LabTestTotal", labTotalAmount);
        totalCostMap.put("UltrasoundTotal", ultrasoundTotalAmount);
        totalCostMap.put("OtherProcedureCost", otherProcedureTotalAmount);
        totalCostMap.put("ConsultationFee", consultationFee);
        totalCostMap.put("TreatmentTotalCost", treatmentTotalCost);
        totalCostMap.put("InvoiceId", BigDecimal.valueOf(invoiceId));


        totalCostMap.put("InvoiceSubtotal", invoiceSubtotal);

        totalCostMap.put("TotalAmountDue", totalAmountDue);

        // from invoice

        totalCostMap.put("Discount", invoiceWithVisitId.discount);
        totalCostMap.put("Tax", invoiceWithVisitId.tax);
        totalCostMap.put("TotalAmount", invoiceWithVisitId.totalAmount);
        totalCostMap.put("AmountPaid", invoiceWithVisitId.amountPaid);
        totalCostMap.put("BalanceDue", invoiceBalanceDue);



        return totalCostMap;
        //return Response.ok(new ResponseMessage("Invoice updated successfully", new InvoiceDTO(invoice))).build();

    }

    @Transactional
    public Response deleteInvoice(Long id) {
        try {
            // Execute the custom SQL query to delete the payment
            int rowsDeleted = invoiceRepository.deleteInvoiceById(id);

            // Check if any rows were deleted
            if (rowsDeleted > 0) {
                return Response.ok(new ResponseMessage(ActionMessages.DELETED.label)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("invoice not found", null))
                        .build();
            }
        } catch (Exception e) {
            // Log the error and return a 500 response
            System.err.println("Error deleting invoice: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseMessage("Failed to delete invoice: " + e.getMessage(), null))
                    .build();
        }
    }


    @Transactional
    public Map<String, BigDecimal> getTotalPatientBalanceDue(Long patientId) {

        List<Invoice> allInvoices = Invoice.find(
                "patient.id = ?1 ORDER BY id DESC",
                patientId
        ).list();

        BigDecimal totalAmountDue = allInvoices.stream()
                .map(invoice -> invoice.balanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return as a map with keys for clarity
        Map<String, BigDecimal> totalCostMap = new HashMap<>();

        totalCostMap.put("TotalAmountDue", totalAmountDue);

        return totalCostMap;
    }




    public static Image getLogo() {
        try {
            // Replace with your logo path or URL
            String logoPath = "src/main/resources/logo.png"; // Local file
            // String logoPath = "https://example.com/logo.png"; // External URL

            // Load the image
            ImageData imageData = ImageDataFactory.create(logoPath);

            // Create the Image object
            Image logo = new Image(imageData);

            // Scale and align the logo
            logo.scaleToFit(80, 80);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

            return logo;
        } catch (IOException | MalformedURLException e) {
            throw new RuntimeException("Failed to load the logo image.", e);
        }
    }



    @Transactional
    public Response generateAndReturnInvoicePdf(Long visitId) {
        try {
            // Find the patient visit
            PatientVisit visit = PatientVisit.findById(visitId);
            if (visit == null) {
                throw new IllegalArgumentException("Visit not found.");
            }

            // Ensure visit.invoice is not null and contains at least one invoice
            if (visit.invoice == null || visit.invoice.isEmpty()) {
                Invoice invoice = createInvoice(visitId); // Create a new invoice
                visit.invoice.add(invoice); // Add invoice to visit (if applicable)
                visit.persist(); // Save the changes

            }


            // Get the first invoice from the list (or handle multiple invoices as needed)
            Invoice invoice = visit.invoice.get(0); // Assuming visit.invoice is a List<Invoice>

            // Ensure the invoice is not null
            if (invoice == null) {
                throw new IllegalArgumentException("Invoice not found.");
            }

            // Create the PDF document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);

            // Add invoice title
            Table invoiceTitle = new Table(new float[]{1});
            invoiceTitle.setWidthPercent(100);
            invoiceTitle.addCell(new Cell()
                    .add(new Paragraph("STATEMENT")
                            .setBold()
                            .setFontSize(11)
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
                    .setPaddingLeft(-27)
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
                    .add(new Paragraph("BUGOGO VILLAGE, KYEGEGWA").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph(invoice.visit.patient.patientFirstName.toUpperCase() + " " + invoice.visit.patient.patientSecondName.toUpperCase())
                            .setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph(invoice.visit.patient.patientAddress.toUpperCase())
                            .setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("NUMBER: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("DUE DATE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("DATE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("BALANCE DUE: ").setFontSize(7).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph(invoice.invoiceNo).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(invoice.dateOfInvoice)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(invoice.dateOfInvoice)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(invoice.balanceDue)).setFontSize(7).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
            );

            document.add(headerTable);

            // Add items table
            float[] columnWidths = {4, 1, 2, 2};
            Table itemsTable = new Table(columnWidths);
            itemsTable.setWidthPercent(100);

            // Add header with no column lines
            itemsTable.addCell(createCell("ITEM", 1, TextAlignment.LEFT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.color.Color.WHITE)
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("QTY", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.color.Color.WHITE)
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("UNIT PRICE (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.color.Color.WHITE)
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK)
                    .setBorder(Border.NO_BORDER));

            itemsTable.addCell(createCell("TOTAL (UGX)", 1, TextAlignment.RIGHT)
                    .setBold()
                    .setFontSize(7)
                    .setFontColor(com.itextpdf.kernel.color.Color.WHITE)
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.BLACK)
                    .setBorder(Border.NO_BORDER));

            // Add table rows
            for (Consultation consultation : invoice.visit.getConsultation()) {
                Border bottomBorder = new SolidBorder(1f);

                itemsTable.addCell(createCell("CONSULTATION FEE", 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(bottomBorder));

                itemsTable.addCell(createCell("1", 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(bottomBorder));

                itemsTable.addCell(createCell(String.valueOf(consultation.consultationFee), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(bottomBorder));

                itemsTable.addCell(createCell(String.valueOf(consultation.consultationFee), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(bottomBorder));
            }

            // Add rows for ProcedureRequested
            boolean isEvenRow = false;
            assert invoice.visit != null;
            for (ProcedureRequested procedureRequested : invoice.visit.getProceduresRequested()) {
                com.itextpdf.kernel.color.Color rowColor = isEvenRow
                        ? com.itextpdf.kernel.color.Color.WHITE
                        : com.itextpdf.kernel.color.Color.LIGHT_GRAY;

                itemsTable.addCell(createCell(procedureRequested.procedure.procedureName.toUpperCase(), 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(procedureRequested.quantity), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(procedureRequested.unitSellingPrice), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(procedureRequested.totalAmount), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                isEvenRow = !isEvenRow;
            }

            // Add rows for TreatmentRequested
            for (TreatmentRequested treatmentRequested : invoice.visit.getTreatmentRequested()) {
                com.itextpdf.kernel.color.Color rowColor = isEvenRow
                        ? com.itextpdf.kernel.color.Color.WHITE
                        : com.itextpdf.kernel.color.Color.LIGHT_GRAY;

                itemsTable.addCell(createCell(treatmentRequested.itemName.toUpperCase(), 1, TextAlignment.LEFT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(treatmentRequested.quantity), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(treatmentRequested.unitSellingPrice), 1, TextAlignment.RIGHT)
                        .setFontSize(7)
                        .setBackgroundColor(rowColor)
                        .setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(1)));

                itemsTable.addCell(createCell(String.valueOf(treatmentRequested.totalAmount), 1, TextAlignment.RIGHT)
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


            Cell notesCell1 = new Cell(6, 1)
                    .add(("NOTES: " +"\n"+"PATIENT NAME: " +invoice.visit.patient.patientFirstName.toUpperCase()+" "+invoice.visit.patient.patientSecondName.toUpperCase() ))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.TOP);
            totalsTable.addCell(notesCell1);

           /* for (Consultation consultation : invoice.visit.getConsultation()) {

            // Add notes
            Cell notesCell = new Cell(6, 1)
                    .add(("NOTES: " +"\n"+"PATIENT NAME: " +invoice.visit.patient.patientFirstName.toUpperCase()+" "+invoice.visit.patient.patientSecondName.toUpperCase() +"\n"+ "DIAGNOSIS: "+consultation.diagnosis.toUpperCase() +"\n"+ consultation.medicalHistory.toUpperCase() +"\n" + invoice.notes.toUpperCase()))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.TOP);
            totalsTable.addCell(notesCell);

            }*/

            // Add subtotal row
            totalsTable.addCell(createCell("SUBTOTAL:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBold()
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.LIGHT_GRAY));
            totalsTable.addCell(createCell(invoice.subTotal.toString(), 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1))
                    .setBold()
                    .setBackgroundColor(com.itextpdf.kernel.color.Color.LIGHT_GRAY));

            // Add discount row
            totalsTable.addCell(createCell("DISCOUNT:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell(invoice.discount.toString(), 1, TextAlignment.RIGHT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));

            // Add tax row
            totalsTable.addCell(createCell("TAX:", 1, TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(1)));
            totalsTable.addCell(createCell(invoice.tax.toString(), 1, TextAlignment.RIGHT)
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
            totalsTable.addCell(createCell(invoice.totalAmount.toString(), 1, TextAlignment.RIGHT)
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
            totalsTable.addCell(createCell(invoice.amountPaid.toString(), 1, TextAlignment.RIGHT)
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
            totalsTable.addCell(createCell(invoice.balanceDue.toString(), 1, TextAlignment.RIGHT)
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











    public Response generateInvoicePdfWithLogo(Long invoiceId) {
        // Retrieve the invoice
        Invoice invoice = Invoice.findById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found for ID: " + invoiceId);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Create PDF writer and document
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add the logo
            try {
                // Replace with your logo path or URL
                String logoPath = "src/main/resources/logo.png"; // Local file
                // String logoPath = "https://example.com/logo.png"; // External URL

                // Load the image
                ImageData imageData = ImageDataFactory.create(logoPath);

                // Create the Image object
                Image logo = new Image(imageData);

                // Scale and align the logo
                logo.scaleToFit(80, 80);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

            } catch (IOException | MalformedURLException e) {
                throw new RuntimeException("Failed to load the logo image.", e);
            }

            // Title
            Paragraph title = new Paragraph("INVOICE")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(title);

            // Invoice Details
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{2, 3}))
                    .useAllAvailableWidth()
                    .setMarginTop(10);
            headerTable.addCell(createCell("Number:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(createCell(invoice.invoiceNo, 3, TextAlignment.LEFT));
            headerTable.addCell(createCell("Reference:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(createCell(invoice.reference, 3, TextAlignment.LEFT));
            headerTable.addCell(createCell("Date:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(createCell(invoice.dateOfInvoice.toString(), 3, TextAlignment.LEFT));
            headerTable.addCell(createCell("Due Date:", 3, TextAlignment.LEFT).setBold());
            headerTable.addCell(createCell(invoice.upDateOfInvoice.toString(), 3, TextAlignment.LEFT));
            document.add(headerTable);

            // Sender and Receiver Information
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                    .useAllAvailableWidth()
                    .setMarginTop(15);
            infoTable.addCell(createCell("FROM:", 3, TextAlignment.LEFT).setBorder(com.itextpdf.layout.border.Border.NO_BORDER));
            infoTable.addCell(createCell("TO:", 3, TextAlignment.LEFT).setBold().setBorder(com.itextpdf.layout.border.Border.NO_BORDER));
            infoTable.addCell(createCell("VENERANDA MEDICAL", 3, TextAlignment.LEFT).setBorder(com.itextpdf.layout.border.Border.NO_BORDER));
            infoTable.addCell(createCell("TIN: ", 3, TextAlignment.LEFT).setBorder(com.itextpdf.layout.border.Border.NO_BORDER));
            infoTable.addCell(createCell(invoice.toName, 3, TextAlignment.LEFT).setBorder(com.itextpdf.layout.border.Border.NO_BORDER));
            infoTable.addCell(createCell("EMAIL: ", 3, TextAlignment.LEFT).setBorder(com.itextpdf.layout.border.Border.NO_BORDER));
            document.add(infoTable);

            // Invoice Items
            Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{4, 1, 1, 1}))
                    .useAllAvailableWidth()
                    .setMarginTop(15)
                    .setBackgroundColor(new DeviceRgb(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(), Color.LIGHT_GRAY.getBlue()));
            itemsTable.addHeaderCell("Item");
            itemsTable.addHeaderCell("Qty");
            itemsTable.addHeaderCell("Unit Price (UGX)");
            itemsTable.addHeaderCell("Total (UGX)");

            for (ProcedureRequested procedureRequested : invoice.visit.getProceduresRequested()) {
                itemsTable.addCell(createCell(procedureRequested.procedure.procedureName, 3, TextAlignment.LEFT));
                itemsTable.addCell(createCell(String.valueOf(procedureRequested.quantity), 3, TextAlignment.RIGHT));
                itemsTable.addCell(createCell(String.valueOf(procedureRequested.unitSellingPrice), 3, TextAlignment.RIGHT));
                itemsTable.addCell(createCell(String.valueOf(procedureRequested.totalAmount), 3, TextAlignment.RIGHT));
            }

            document.add(itemsTable);

            // Summary
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                    .useAllAvailableWidth()
                    .setMarginTop(10);
            summaryTable.addCell(createCell("Discount", 3, TextAlignment.LEFT).setBold());
            summaryTable.addCell(createCell(invoice.discount.toString(), 3, TextAlignment.RIGHT));
            summaryTable.addCell(createCell("Total", 3, TextAlignment.LEFT).setBold());
            summaryTable.addCell(createCell(invoice.totalAmount.toString(), 3, TextAlignment.RIGHT));
            document.add(summaryTable);

            // Notes
            if (invoice.notes != null && !invoice.notes.isEmpty()) {
                document.add(new Paragraph("NOTES").setBold().setMarginTop(10));
                document.add(new Paragraph(invoice.notes));
            }

            // Close the document
            document.close();

            // Convert to byte array and return response
            byte[] pdfBytes = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=invoice_" + invoice.invoiceNo + ".pdf")
                    .type("application/pdf")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice PDF", e);
        }
    }

    // Utility method to create cells with alignment
    private Cell createCell(String content, int i, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setTextAlignment(alignment);
        return cell;
    }


   /* @Transactional
    public Response generateAndReturnInvoicePdf(Long invoiceId) {
        Invoice invoice = Invoice.findById(invoiceId);

        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found.");
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);

            Document document = new Document(pdfDocument);

            Table table = new Table(6);
            table.setWidthPercent(100);

            Cell[] headerCells = {
                    createCell("Number"),
                    createCell("Category"),
                    createCell("Title"),
                    createCell("Description"),
                    createCell("CostPrice"),
                    createCell("Creation Date")

            };

            for (Cell cell : headerCells) {
                cell.setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }

            for (FullShopItemResponse Item : getShopItemsAdvancedFilter(request)) {
                table.addCell(createCell(Item.number));
                table.addCell(createCell(Item.category));
                table.addCell(createCell(Item.title));
                table.addCell(createCell(Item.description));
                table.addCell(createCell("$" + Item.costPrice.toString()));
                table.addCell(createCell(Item.creationDate.toString()));
            }

            document.add(table);

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return Response.ok(new ByteArrayInputStream(pdfBytes))
                    .header("Content-Disposition", "attachment; filename=shop_items.pdf")
                    .type("application/pdf")
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Cell createCell(String content) {
        return new Cell().add(content);
    }



    public List<FullShopItemResponse> getShopItemsAdvancedFilter(ShopItemParametersRequest request) {
        StringJoiner whereClause = getStringJoiner(request);

        String sql = """
                 
                    SELECT
                    id,
                    category,
                    number,
                    image,
                    title,
                    costPrice,
                    sellingPrice,
                    creationDate,
                    unitOfMeasure,
                    description
                    FROM item
                    %s
                    ORDER BY creationDate DESC;                             
                    """.formatted(whereClause);

        return client.query(sql)
                .execute()
                .onItem()
                .transformToMulti(rows -> Multi.createFrom().iterable(rows))
                .onItem()
                .transform(this::from)
                .collect().asList().await()
                .indefinitely();

    }

    private FullShopItemResponse from(Row row){

        FullShopItemResponse response = new FullShopItemResponse();
        response.id = row.getLong("id");
        response.description = row.getString("description");
        response.image = row.getString("image");
        response.number = row.getString("number");
        response.category = row.getString("category");
        response.title = row.getString("title");
        response.costPrice = row.getBigDecimal("costPrice");
        response.sellingPrice = row.getBigDecimal("sellingPrice");
        response.unitOfMeasure = row.getString("unitOfMeasure");
        response.creationDate = row.getLocalDate("creationDate");

        return response;
    }

    private FullShopItemResponse fullShopItemDTO(Item Item){
        FullShopItemResponse response = new FullShopItemResponse();
        response.id = Item.id;
        response.number = Item.number;
        response.costPrice = Item.costPrice;
        response.sellingPrice = Item.sellingPrice;
        response.description = Item.description;
        response.category = Item.category;
        response.unitOfMeasure = Item.unitOfMeasure;
        response.title = Item.title;
        response.creationDate =Item.creationDate;
        response.image = Item.image;

        return response;
    }

    private StringJoiner getStringJoiner(ShopItemParametersRequest request) {
        AtomicReference<Boolean> hasSearchCriteria = new AtomicReference<>(Boolean.FALSE);

        List<String> conditions = new ArrayList<>();
        if (request.category != null && !request.category.isEmpty()) {
            conditions.add("category = '" + request.category + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.title != null && !request.title.isEmpty()) {
            conditions.add("title = '" + request.title + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        if (request.datefrom != null && request.dateto != null) {
            conditions.add("creationDate BETWEEN '" + request.datefrom + "' AND '" + request.dateto + "'");
            hasSearchCriteria.set(Boolean.TRUE);
        }

        StringJoiner whereClause = new StringJoiner(" AND ", "WHERE ", "");

        conditions.forEach(whereClause::add);

        if (Boolean.FALSE.equals(hasSearchCriteria.get())) {
            whereClause.add("1 = 1");
        }

        return whereClause;
    }*/

















}
