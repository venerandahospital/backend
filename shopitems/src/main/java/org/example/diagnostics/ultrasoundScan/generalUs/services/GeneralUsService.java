package org.example.diagnostics.ultrasoundScan.generalUs.services;

import com.itextpdf.io.IOException;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;

import org.example.client.domains.PatientGroup;
import org.example.configuration.handler.ResponseMessage;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.repositories.GeneralUsRepository;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsUpdateRequest;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.responses.GeneralUsDTO;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.GeneralUs;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsRequest;
import org.example.finance.invoice.domains.Invoice;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.treatment.domains.TreatmentRequested;
import org.example.visit.domains.PatientVisit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;


import com.itextpdf.layout.property.*;

import java.awt.*;

import java.util.*;

import static org.example.finance.invoice.services.InvoiceService.getLogo;

@ApplicationScoped
public class GeneralUsService {

    @Inject
    GeneralUsRepository generalUsRepository;

    @Inject
    MySQLPool client;


    public static final String NOT_FOUND = "Not found!";

        @Transactional
        public Response createGeneralUsReport(GeneralUsRequest request){

            PatientVisit patientVisit = PatientVisit.findById(request.visitId); // ✅ correct
            if (patientVisit == null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("Patient visit not found for ID: " + request.visitId))
                        .build();
            }

            Patient patient = patientVisit.patient;

            ProcedureRequested procedureRequested = ProcedureRequested.findById(request.procedureRequestedId); // ✅ correct
            if (procedureRequested == null) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("procedureRequested not found for ID: " + request.procedureRequestedId))
                        .build();
            }

            GeneralUs generalUs = new GeneralUs();
            generalUs.patientName = patient.patientFirstName +" "+ patient.patientSecondName;
            generalUs.gender = patient.patientGender;
            generalUs.patientAge = patient.patientAge;
            generalUs.visit = patientVisit;
            generalUs.procedureRequested = procedureRequested;
            generalUs.indication = request.indication;
            generalUs.doneBy = request.doneBy;
            generalUs.recommendation = request.recommendation;

            generalUs.exam = request.exam;
            generalUs.findings = request.findings;
            generalUs.impression = request.impression;

            generalUs.scanPerformingDate = LocalDate.now();
            generalUs.upDatedDate = LocalDate.now();
            generalUs.timeOfProcedure = LocalTime.now();
            generalUs.scanRequestDate = procedureRequested.dateOfProcedure;


            generalUsRepository.persist(generalUs);


            return Response.status(Response.Status.CREATED)
                    .entity(new ResponseMessage("Scan report created successfully", new GeneralUsDTO(generalUs)))
                    .build();


        }

    @Transactional
    public Response updateScanReportById(Long id, GeneralUsUpdateRequest request) {
        GeneralUs generalUs = GeneralUs.findById(id); // ✅ correct
        if (generalUs == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("Scan Report not found for ID: " + id))
                    .build();
        }

        generalUs.indication = request.indication;
        generalUs.doneBy = request.doneBy;
        generalUs.recommendation = request.recommendation;

        generalUs.exam = request.exam;
        generalUs.findings = request.findings;
        generalUs.impression = request.impression;

        generalUs.upDatedDate = LocalDate.now();

        generalUsRepository.persist(generalUs);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Scan report updated successfully", new GeneralUsDTO(generalUs)))
                .build();


    }


    public static com.itextpdf.layout.element.Image getLogo() {
        try {
            // Load the image as a stream from the classpath
            InputStream logoStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("logo.png");

            if (logoStream == null) {
                throw new RuntimeException("Logo image not found in classpath.");
            }

            byte[] imageBytes = logoStream.readAllBytes(); // Java 9+; use IOUtils for Java 8
            ImageData imageData = ImageDataFactory.create(imageBytes);

            com.itextpdf.layout.element.Image logo = new Image(imageData);
            logo.scaleToFit(80, 80);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

            return logo;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the logo image.", e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Transactional
    public Response generateAndReturnScanReportPdf(Long generalUsId) {
        try {
            GeneralUs generalUs = GeneralUs.findById(generalUsId);
            if (generalUs == null) {
                //throw new IllegalArgumentException("scan not found.");
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("Scan Report not found for ID: " + generalUsId))
                        .build();
            }

            // Find the patient visit
            ProcedureRequested procedureRequestedScan = ProcedureRequested.findById(generalUs.procedureRequested.id);
            if (procedureRequestedScan == null) {
                //throw new IllegalArgumentException("Visit not found.");
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("procedure requested not found for ID: " + generalUs.procedureRequested.id))
                        .build();
            }

            // Find the patient visit
             Patient patient = Patient.findById(generalUs.visit.patient.id);
            if (patient == null) {
                //throw new IllegalArgumentException("Visit not found.");
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("patient not found for ID: " + generalUs.visit.patient.id))
                        .build();
            }

            // Find the patient visit
            PatientVisit visit = PatientVisit.findById(generalUs.visit.id);
            if (visit == null) {
                //throw new IllegalArgumentException("Visit not found.");
                return Response.status(Response.Status.CONFLICT)
                        .entity(new ResponseMessage("visit not found for ID: " + generalUs.visit.id))
                        .build();
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
                    .add(new Div()
                            .setBorderBottom(new SolidBorder(1)) // Underline (1px solid line)
                            .setPaddingBottom(2) // Space between text and underline
                            .add(new Paragraph("GENERAL ULTRASOUND SCAN REPORT")
                                    .setBold()
                                    .setFontSize(11)
                                    .setTextAlignment(TextAlignment.CENTER)
                            )
                    )
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(15)
                    .add(new Paragraph("Department of Medical Diagnostics Veneranda Hospital")
                            .setFontSize(10)
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
                    .add(new Paragraph()
                            .add(Optional.ofNullable(invoice.visit.patient.getPatientGroup())
                                    .map(PatientGroup::getGroupName)
                                    .map(String::toUpperCase)
                                    .orElse(""))
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT))

                    .add(new Paragraph()
                            .add(Optional.ofNullable(invoice.visit.patient.patientAddress)
                                    .map(String::toUpperCase)
                                    .orElse(""))
                            .setFontSize(7)
                            .setTextAlignment(TextAlignment.LEFT)
                    )




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
            /*for (Consultation consultation : invoice.visit.getConsultation()) {
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
            }*/

            // Add rows for ProcedureRequested
            boolean isEvenRow = false;
            assert invoice.visit != null;
            for (ProcedureRequested procedureRequested : invoice.visit.getProceduresRequested()) {
                com.itextpdf.kernel.color.Color rowColor = isEvenRow
                        ? com.itextpdf.kernel.color.Color.WHITE
                        : com.itextpdf.kernel.color.Color.LIGHT_GRAY;

                itemsTable.addCell(createCell(procedureRequested.procedureRequestedType.toUpperCase(), 1, TextAlignment.LEFT)
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

                itemsTable.addCell(createCell(treatmentRequested.quantity.toString(), 1, TextAlignment.RIGHT)
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


            Cell notesCell1 = new Cell(6, 1) // 6 rows tall, 1 column wide

                            // FINDINGS section
                            .add(new Paragraph()
                                    .add(new Text("FINDINGS:")
                                            .setUnderline()
                                            .setBold()  // Only the title is bold
                                    )
                                    .setFontSize(12)
                                    .setTextAlignment(TextAlignment.LEFT)
                            )
                            // FINDINGS content (normal weight)
                            .add(new Paragraph(generalUs.findings)
                                    .setFontSize(12)
                                    .setTextAlignment(TextAlignment.LEFT)
                            )
                            .add("\n")
                            // IMPRESSION section
                            .add(new Paragraph()
                                    .add(new Text("IMPRESSION:")
                                            .setUnderline()
                                            .setBold()  // Only the title is bold
                                    )
                                    .setFontSize(12)
                                    .setTextAlignment(TextAlignment.LEFT)
                            )
                            .add("\n")
                            // IMPRESSION content (normal weight)
                            .add(new Paragraph(generalUs.impression)
                                    .setFontSize(12)
                                    .setTextAlignment(TextAlignment.LEFT)
                            )
                            .add("\n")
                            // Recommendation section
                            .add(new Paragraph()
                                    .add(new Text("Recommendations:")
                                            .setUnderline()
                                            .setBold()  // Only the title is bold
                                    )
                                    .setFontSize(12)
                                    .setTextAlignment(TextAlignment.LEFT)
                            )
                            .add("\n")
                            // IMPRESSION content (normal weight)
                            .add(new Paragraph(generalUs.recommendation)
                                    .setFontSize(12)
                                    .setTextAlignment(TextAlignment.LEFT)
                            )
                            .add("\n")
                            .add(new Table(new float[]{1, 1}) // 2 equal columns
                                    .setWidth(UnitValue.createPercentValue(100))
                                    .setBorder(Border.NO_BORDER)
                                    .addCell(new Cell()
                                            .add(new Paragraph("Imaging Technologist/Sonographer/Radiographer:")
                                                    .setUnderline()
                                                    .setBold()
                                            )
                                            .setBorder(Border.NO_BORDER)
                                            .setTextAlignment(TextAlignment.LEFT)
                                    )
                                    .addCell(new Cell()
                                            .add(new Paragraph("Signature/Stamp:")
                                                    .setUnderline()
                                                    .setBold()
                                            )
                                            .setBorder(Border.NO_BORDER)
                                            .setTextAlignment(TextAlignment.RIGHT)
                                    )
                                    .setFontSize(12)
                            )
                            .add("\n")
                            .add(new Table(new float[]{1, 1}) // 2 equal columns
                                    .setWidth(UnitValue.createPercentValue(100))
                                    .setBorder(Border.NO_BORDER)
                                    .addCell(new Cell()
                                            .add(new Paragraph(generalUs.doneBy)


                                            )
                                            .setBorder(Border.NO_BORDER)
                                            .setTextAlignment(TextAlignment.LEFT)
                                    )
                                    .addCell(new Cell()
                                            .add(new Paragraph(".............................................")


                                            )
                                            .setBorder(Border.NO_BORDER)
                                            .setTextAlignment(TextAlignment.RIGHT)
                                    )
                                    .setFontSize(12)

                    )
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.TOP);

            totalsTable.addCell(notesCell1);



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

    // Helper methods (unchanged)
    private Paragraph createSectionHeader(String text) {
        return new Paragraph()
                .add(new Text(text)
                        .setUnderline()
                        .setBold()
                        .setFontSize(12)
                )
                .setTextAlignment(TextAlignment.LEFT);
    }

    private Paragraph createSectionContent(String content) {
        return new Paragraph(content)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(5);
    }



























}
