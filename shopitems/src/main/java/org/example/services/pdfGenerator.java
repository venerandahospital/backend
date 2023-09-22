package org.example.services;

import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.FileNotFoundException;

public class pdfGenerator {

    public static void main(String[] args) throws FileNotFoundException {
        String outputPath = "output.pdf";

        PdfWriter pdfWriter = new PdfWriter(outputPath);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);
        document.add(new Paragraph("hello mia this my first pdf1"));
        document.add(new Paragraph("hello mia this my first pdf2"));
        document.add(new Paragraph("hello mia this my first pdf3"));
        document.add(new Paragraph("hello mia this my first pdf4"));
        document.add(new Paragraph("hello mia this my first pdf5"));

        document.close();

    }
















    /*public void pdfGenerator() throws FileNotFoundException {
        String outputPath = "output.pdf";

        // Create a PdfWriter instance to write to the file
        try (PdfWriter pdfWriter = new PdfWriter(outputPath);
             PdfDocument pdfDocument = new PdfDocument(pdfWriter)) {

            // Create a Document instance for adding content
            Document document = new Document(pdfDocument);

            // Add content to the document
            document.add(new Paragraph("Hello, iText 8.0.1!")
                    .setFont(PdfFontFactory.createFont())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));

            System.out.println("PDF created successfully at: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
