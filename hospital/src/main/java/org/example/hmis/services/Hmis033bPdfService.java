package org.example.hmis.services;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.hmis.services.payloads.Hmis033bAggregateResponse;

@ApplicationScoped
public class Hmis033bPdfService {

    private static final String TEMPLATE = "/hmis/033b-template.pdf";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final float FONT_SIZE = 9f;

    private static final Map<String, Integer> DISEASE_ROW = Map.ofEntries(
            Map.entry("MA.", 0), Map.entry("DY.", 1), Map.entry("SA.", 2), Map.entry("AF.", 3),
            Map.entry("AE.", 4), Map.entry("AB.", 5), Map.entry("MG.", 6), Map.entry("CH.", 7),
            Map.entry("GW.", 8), Map.entry("ME.", 9), Map.entry("NT.", 10), Map.entry("PL.", 11),
            Map.entry("TF.", 12), Map.entry("HB.", 13), Map.entry("DR.", 14), Map.entry("YF.", 15),
            Map.entry("VF.", 16), Map.entry("LP.", 17), Map.entry("AX.", 18), Map.entry("CV.", 19));

    @Inject Hmis033bAggregationService aggregationService;

    public byte[] generate(LocalDate from, LocalDate to) {
        Hmis033bAggregateResponse data = aggregationService.aggregate(from, to);
        try (InputStream template = openTemplate();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfReader reader = new PdfReader(template);
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(reader, writer);
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            overlayHeader(pdf, font, data);
            overlayDiseases(pdf, font, data);
            overlayOpd(pdf, font, data);
            overlayMalaria(pdf, font, data);
            overlayTracerStock(pdf, font, data);

            pdf.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate HMIS 033b PDF", ex);
        }
    }

    private InputStream openTemplate() {
        InputStream stream = Hmis033bPdfService.class.getResourceAsStream(TEMPLATE);
        if (stream == null) {
            throw new IllegalStateException("Missing HMIS template: " + TEMPLATE);
        }
        return stream;
    }

    private void overlayHeader(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfPage page = pdf.getPage(3);
        PdfCanvas canvas = new PdfCanvas(page);
        Hmis033bAggregateResponse.Period period = data.period;
        Hmis033bAggregateResponse.FacilityHeader facility = data.facility;

        draw(canvas, font, 95, 808, period.weekNumber != null ? String.valueOf(period.weekNumber) : "");
        draw(canvas, font, 210, 808, safe(facility.name));
        draw(canvas, font, 430, 808, safe(facility.code));
        draw(canvas, font, 95, 793, safe(facility.parish));
        draw(canvas, font, 210, 793, safe(facility.subCounty));
        draw(canvas, font, 350, 793, safe(facility.hsd));
        draw(canvas, font, 470, 793, safe(facility.district));
        draw(canvas, font, 120, 823, period.from != null ? period.from.format(DATE_FMT) : "");
        draw(canvas, font, 220, 823, period.to != null ? period.to.format(DATE_FMT) : "");
        draw(canvas, font, 430, 823, period.reportDate != null ? period.reportDate.format(DATE_FMT) : "");
    }

    private void overlayDiseases(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfPage page = pdf.getPage(3);
        PdfCanvas canvas = new PdfCanvas(page);
        Map<String, Hmis033bAggregateResponse.DiseaseLine> byCode = new HashMap<>();
        if (data.diseases != null) {
            for (Hmis033bAggregateResponse.DiseaseLine line : data.diseases) {
                if (line != null && line.code != null) {
                    byCode.put(line.code, line);
                }
            }
        }
        float firstRowY = 662f;
        float rowStep = 16.2f;
        float casesX = 300f;
        float deathsX = 355f;
        float testedX = 410f;
        float positiveX = 465f;

        for (Map.Entry<String, Integer> entry : DISEASE_ROW.entrySet()) {
            Hmis033bAggregateResponse.DiseaseLine line = byCode.get(entry.getKey());
            if (line == null) {
                continue;
            }
            float y = firstRowY - (entry.getValue() * rowStep);
            draw(canvas, font, casesX, y, intOrBlank(line.casesThisWeek));
            draw(canvas, font, deathsX, y, intOrBlank(line.deathsThisWeek));
            draw(canvas, font, testedX, y, intOrBlank(line.testedCases));
            draw(canvas, font, positiveX, y, intOrBlank(line.positiveCases));
        }
    }

    private void overlayOpd(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfPage page = pdf.getPage(4);
        PdfCanvas canvas = new PdfCanvas(page);
        draw(canvas, font, 170, 705, intOrBlank(data.opd.newAttendance));
        draw(canvas, font, 330, 705, intOrBlank(data.opd.totalAttendance));
        draw(canvas, font, 170, 688, intOrBlank(data.referrals.referralsOut));
        draw(canvas, font, 330, 688, intOrBlank(data.referrals.referralsIn));
    }

    private void overlayMalaria(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfPage page = pdf.getPage(5);
        PdfCanvas canvas = new PdfCanvas(page);
        Hmis033bAggregateResponse.MalariaSummary m = data.malaria;
        float y = 718f;
        draw(canvas, font, 55, y, intOrBlank(m.suspectedFever));
        draw(canvas, font, 145, y, intOrBlank(m.testedRdt));
        draw(canvas, font, 235, y, intOrBlank(m.rdtPositive));
        draw(canvas, font, 325, y, intOrBlank(m.rdtPositiveTreated));
        draw(canvas, font, 415, y, intOrBlank(m.testedMicroscopy));
        draw(canvas, font, 505, y, intOrBlank(m.microscopyPositive));
        draw(canvas, font, 145, 700f, intOrBlank(m.microscopyPositiveTreated));
        draw(canvas, font, 325, 700f, intOrBlank(m.notTestedTreated));
    }

    private void overlayTracerStock(PdfDocument pdf, PdfFont font, Hmis033bAggregateResponse data) {
        PdfPage page = pdf.getPage(6);
        PdfCanvas canvas = new PdfCanvas(page);
        if (data.tracerStock == null) {
            return;
        }
        float y = 705f;
        float step = 14f;
        int index = 0;
        for (Hmis033bAggregateResponse.TracerStockLine line : data.tracerStock) {
            if (line == null) {
                continue;
            }
            String label = line.tracerCode != null ? line.tracerCode : line.tracerName;
            draw(canvas, font, 470, y - (index * step), formatBalance(line.balance));
            index++;
            if (index >= 8) {
                break;
            }
        }
    }

    private void draw(PdfCanvas canvas, PdfFont font, float x, float y, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        canvas.beginText()
                .setFontAndSize(font, FONT_SIZE)
                .moveText(x, y)
                .showText(text)
                .endText();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String intOrBlank(int value) {
        return value == 0 ? "" : String.valueOf(value);
    }

    private String formatBalance(double value) {
        if (value == 0d) {
            return "";
        }
        if (Math.rint(value) == value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}