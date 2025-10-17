package org.example.diagnostics.ultrasoundScan.generalUs.services;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;

public class FooterHelperUsReport implements IEventHandler {

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        Rectangle pageSize = page.getPageSize();

        // Create canvas for footer (using newContentStreamAfter() to ensure it's on top)
        PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);

        try (Canvas canvas = new Canvas(pdfCanvas, pdfDoc, pageSize)) {
            // Footer container with background (optional)
            Div footerContainer = new Div()
                    .setWidth(pageSize.getWidth() - 72) // Account for margins
                    .setFixedPosition(
                            pageSize.getLeft() + 36,  // Left margin
                            54,                       // Increased bottom margin (54 points = 0.75 inch)
                            pageSize.getWidth() - 72   // Width accounting for margins
                    );

            // Main footer text
            /*Paragraph mainFooter = new Paragraph()
                    .add("- Department of Medical Diagnostics - For any Inquires call the Radiographer on 0784411848 - System Generated -")
                    .setFontSize(8)
                    .setMarginBottom(3)
                    .setTextAlignment(TextAlignment.CENTER);*/

            // Contact information
            Paragraph inquiries = new Paragraph()
                    .add("For any inquiries or suggestions about the report call on Tel: +256 784 411 848 or +256 7049 687 36 or email us at venerandamedical@gmail.com")
                    .setFontSize(8)
                    .setMarginBottom(1)
                    .setTextAlignment(TextAlignment.CENTER);

            // Contact information
            Paragraph contactInfo = new Paragraph()
                    .add("Bugogo Town Council, Kyegegwa District | Tel: +256 784 411 848 | Email: venerandahospital@gmail.com")
                    .setFontSize(7)
                    .setMarginBottom(1)
                    .setTextAlignment(TextAlignment.CENTER);

            // Thank you message
            Paragraph thankYou = new Paragraph()
                    .add("Thank you for choosing Veneranda Hospital!")
                    .setFontSize(6)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER);

            // Add all elements to footer container
            footerContainer.add(inquiries);
            footerContainer.add(contactInfo);
            footerContainer.add(thankYou);

            // Add footer container to canvas
            canvas.add(footerContainer);

            // Optional: Add a separator line above footer (position adjusted higher)
            pdfCanvas.setStrokeColor(Color.BLACK)
                    .setLineWidth(0.5f)
                    .moveTo(pageSize.getLeft() + 36, 72)  // 72 points from bottom (1 inch)
                    .lineTo(pageSize.getRight() - 36, 72)  // 72 points from bottom (1 inch)
                    .stroke();
        }
        pdfCanvas.release();
    }
}