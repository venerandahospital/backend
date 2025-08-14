package org.example.diagnostics.ultrasoundScan.generalUs.services;

import com.itextpdf.io.IOException;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.PdfDocumentEvent;
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
import io.quarkus.panache.common.Sort;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;

import org.example.client.domains.PatientGroup;
import org.example.configuration.handler.ResponseMessage;
import org.example.consultations.services.payloads.responses.ConsultationDTO;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.repositories.GeneralUsRepository;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsUpdateRequest;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.responses.GeneralUsDTO;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.GeneralUs;
import org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.requests.GeneralUsRequest;
import org.example.finance.invoice.domains.Invoice;
import org.example.finance.invoice.services.payloads.responses.InvoiceDTO;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.procedure.procedureRequested.domains.repositories.ProcedureRequestedRepository;
import org.example.visit.domains.PatientVisit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GeneralUsService {

    @Inject
    GeneralUsRepository generalUsRepository;

    @Inject
    ProcedureRequestedRepository procedureRequestedRepository;

    @Inject
    MySQLPool client;


    public static final String NOT_FOUND = "Not found!";

        @Transactional
        public void createGeneralUsReport(ProcedureRequested procedureRequested){

            PatientVisit patientVisit = procedureRequested.visit; // ✅ correct

            Patient patient = patientVisit.patient;

            GeneralUs generalUs = new GeneralUs();
            generalUs.patientName = patient.patientFirstName +" "+ patient.patientSecondName;
            generalUs.gender = patient.patientGender;
            generalUs.patientAge = patient.patientAge;
            generalUs.visit = procedureRequested.visit;
            generalUs.procedureRequested = procedureRequested;
            generalUs.indication = "please type scan report indication";
            generalUs.doneBy = "Imaging Technologist";
            generalUs.recommendation = "please type scan report recommendation here";
            generalUs.scanReportTitle = "";

            generalUs.exam = procedureRequested.procedureRequestedType;;
            generalUs.findings = "please type scan report findings";
            generalUs.impression = "please type scan report impression";

            generalUs.scanPerformingDate = LocalDate.now();
            generalUs.upDatedDate = LocalDate.now();
            generalUs.timeOfProcedure = LocalTime.now();
            generalUs.scanRequestDate = procedureRequested.dateOfProcedure;


            generalUsRepository.persist(generalUs);


            procedureRequestedRepository.persist(procedureRequested);

        }

    public List<GeneralUsDTO> getAllGeneralUs() {
        return generalUsRepository.listAll(Sort.descending("id"))
                .stream()
                .map(GeneralUsDTO::new)
                .toList();
    }

    @Transactional
    public Response updateScanReportById(Long id, GeneralUsUpdateRequest request) {
        GeneralUs generalUs = GeneralUs.findById(id); // ✅ correct
        if (generalUs == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ResponseMessage("Scan Report not found for ID: " + id))
                    .build();
        }

        ProcedureRequested procedureRequested = ProcedureRequested.findById(generalUs.procedureRequested.id);

        generalUs.indication = request.indication;
        generalUs.doneBy = request.doneBy;
        generalUs.recommendation = request.recommendation;
        generalUs.scanReportTitle = request.scanReportTitle;
        generalUs.exam = request.exam;
        generalUs.findings = request.findings;
        generalUs.impression = request.impression;
        generalUs.scanPerformingDate = LocalDate.now();
        generalUs.timeOfProcedure = LocalTime.now();

        generalUs.upDatedDate = LocalDate.now();

        generalUsRepository.persist(generalUs);

        procedureRequested.report = request.scanReportTitle;
        procedureRequested.status = "Done";

        procedureRequestedRepository.persist(procedureRequested);

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseMessage("Scan report updated successfully", new GeneralUsDTO(generalUs)))
                .build();


    }



    @Transactional
    public Response getScanReportByRequestId(Long procedureRequestedId){


     GeneralUs generalUs = GeneralUs.find("procedureRequested.id", procedureRequestedId).firstResult();
        if (generalUs == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ResponseMessage("Scan Report not found for procedureRequest ID: " + procedureRequestedId))
                    .build();
        }else {
           /*return Response.status(Response.Status.FOUND)
                   .entity(new ResponseMessage("scan report fetched successfully: ", new GeneralUsDTO(generalUs)))
                   .build();*/
           return Response.ok(new ResponseMessage("scan report fetched successfully", new GeneralUsDTO(generalUs))).build();

        }


    }



    @Transactional
    public Response generateAndReturnScanReportPdf(Long procedureRequestedId) {

        try {
             // Find the patient visit
            ProcedureRequested procedureRequestedScan = ProcedureRequested.findById(procedureRequestedId);
            if (procedureRequestedScan == null) {
                //throw new IllegalArgumentException("Visit not found.");
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("procedure requested not found for ID: " + procedureRequestedId))
                        .build();
            }
            GeneralUs generalUs = GeneralUs.find("procedureRequested.id", procedureRequestedId).firstResult();
            if (generalUs == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("Scan Report not found for procedure ID: " + procedureRequestedId))
                        .build();
            }


            // Find the patient visit
             Patient patient = Patient.findById(generalUs.visit.patient.id);
            if (patient == null) {
                //throw new IllegalArgumentException("Visit not found.");
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("patient not found for ID: " + generalUs.visit.patient.id))
                        .build();
            }

            // Find the patient visit
            PatientVisit visit = PatientVisit.findById(generalUs.visit.id);
            if (visit == null) {
                //throw new IllegalArgumentException("Visit not found.");
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("visit not found for ID: " + generalUs.visit.id))
                        .build();
            }



            // Get the first invoice from the list (or handle multiple invoices as needed)
            Invoice invoice = visit.invoice.get(0); // Assuming visit.invoice is a List<Invoice>

            // Ensure the invoice is not null
            if (invoice == null) {
                //throw new IllegalArgumentException("Invoice not found.");
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseMessage("invoice not found for ID: "))
                        .build();
            }

            // Create the PDF document
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);

            pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHelperUsReport());

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
                    .add(new Paragraph("Department of Medical Diagnostics - Veneranda Medical (A subsidiary of Veneranda Hospital)")
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
                    .setPaddingLeft(-27)
            );

            // Add invoice details
            headerTable.addCell(new Cell()
                    .add(new Paragraph("CLIENT NAME: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("ADDRESS: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("EXAM: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))

                    .add(new Paragraph("NEXT OF KEEN: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))

                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph(generalUs.patientName.toUpperCase()).setFontSize(8).setTextAlignment(TextAlignment.LEFT))

                    .add(new Paragraph()
                            .add(Optional.ofNullable(invoice.visit.patient.getPatientGroup())
                                    .map(PatientGroup::getGroupName)
                                    .map(String::toUpperCase)
                                    .orElse(""))
                            .setFontSize(8)
                            .setTextAlignment(TextAlignment.LEFT))

                    .add(new Paragraph()
                            .add(Optional.ofNullable(invoice.visit.patient.patientAddress)
                                    .map(String::toUpperCase)
                                    .orElse(""))
                            .setFontSize(8)
                            .setTextAlignment(TextAlignment.LEFT)
                    )
                    .add(new Paragraph(generalUs.exam.toUpperCase())
                            .setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    //.add(new Paragraph(patient.nextOfKinName.toUpperCase()).setFontSize(8).orElse("N/A")).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph()
                            .add(Optional.ofNullable(patient.nextOfKinName)
                                    .map(String::toUpperCase)
                                    .orElse("N/A"))  // Fallback value if null
                            .setFontSize(8)
                            .setTextAlignment(TextAlignment.LEFT)
                    )






                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("GENDER: "+ generalUs.gender.toUpperCase()).setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("CONTACT: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))

                    .add(new Paragraph("REQUEST DATE: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .add(new Paragraph("PROCEDURE DATE: ").setFontSize(8).setTextAlignment(TextAlignment.LEFT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.addCell(new Cell()
                    .add(new Paragraph("AGE: "+ generalUs.patientAge  + "YRS").setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(patient.patientContact)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))

                    .add(new Paragraph(String.valueOf(generalUs.scanRequestDate)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph(String.valueOf(generalUs.scanPerformingDate)).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
            );

            headerTable.setBorderBottom(new SolidBorder(1));


            document.add(headerTable);




            // Add totals table
            Table totalsTable = new Table(new float[]{4, 2, 2});
            totalsTable.setWidthPercent(100);


            Cell notesCell1 = new Cell(6, 1)
                    // FINDINGS section
                    .add(new Div()
                            .setBorderBottom(new SolidBorder(1)) // Add 1px bottom border

                            .setPaddingBottom(2) // Space between text and border
                            .add(new Paragraph()
                                    .add(new Text("INDICATION: ")
                                            .setBold() // Make only the label bold
                                    )
                                    .add(generalUs.indication.toUpperCase()) // Non-bold content
                                    .setFontSize(9)
                                    .setTextAlignment(TextAlignment.CENTER)
                            )
                    )// 6 rows tall, 1 column wide
                    .add("\n")

                            // FINDINGS section
                            .add(new Paragraph()
                                    .add(new Text("FINDINGS:")
                                            .setUnderline()
                                            .setBold()  // Only the title is bold
                                    )
                                    .setFontSize(10)
                                    .setTextAlignment(TextAlignment.LEFT)
                            )
                            // FINDINGS content (normal weight)
                            .add(new Paragraph(generalUs.findings)
                                    .setFontSize(12)
                                    .setTextAlignment(TextAlignment.LEFT)
                            )
                    .add("\n")
   





                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(7)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.TOP);


            totalsTable.addCell(notesCell1);



            document.add(totalsTable);

            // Create a 2-column table with 60-40 width ratio
            Table impressionTable = new Table(new float[]{3, 2});
            impressionTable.setWidthPercent(100);
            impressionTable.setMarginBottom(15);

            impressionTable.addCell(new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5)
                    .add(new Paragraph("IMPRESSION:")
                            .setUnderline()
                            .setBold()
                            .setFontSize(10)
                    )
                    .add(new Paragraph(generalUs.impression)
                            .setFontSize(12)
                            .setMarginTop(5)
                    )
            );

// Right Column - RECOMMENDATIONS
            impressionTable.addCell(new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5)
                    .add(new Paragraph("RECOMMENDATIONS:")
                            .setUnderline()
                            .setBold()
                            .setFontSize(10)
                    )
                    .add(new Paragraph(generalUs.recommendation)
                            .setFontSize(12)
                            .setMarginTop(5)
                    )
            );

            document.add(impressionTable);

            // Create a 2-column table with 60-40 width ratio
            Table initialsTable = new Table(new float[]{3, 2});
            initialsTable.setWidthPercent(100);
            initialsTable.setMarginBottom(15);

// Left Column - IMPRESSION
            initialsTable.addCell(new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5)
                    .add(new Paragraph("Imaging Technologist / Sonographer / Radiographer:")
                            .setUnderline()
                            .setBold()
                            .setFontSize(10)
                    )
                    .add(new Paragraph(generalUs.doneBy)
                            .setFontSize(12)
                            .setMarginTop(5)
                    )
            );

// Right Column - RECOMMENDATIONS
            initialsTable.addCell(new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5)
                    .add(new Paragraph("Signature/Stamp")
                            .setUnderline()
                            .setBold()
                            .setFontSize(10)
                    )
                    .add(new Paragraph(".............................................")
                            .setFontSize(12)
                            .setMarginTop(5)
                    )
            );

            document.add(initialsTable);

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




    public static com.itextpdf.layout.element.Image getLogo() {
        try {
            // Load the image as a stream from the classpath
            InputStream logoStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("logo-modified.png");

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
























}
