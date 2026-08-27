package org.example.visit.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;
import org.example.client.domains.Patient;
import org.example.client.domains.PatientGroup;
import org.example.client.domains.repositories.PatientGroupRepository;
import org.example.consultations.domains.Consultation;
import org.example.finance.invoice.domains.Invoice;
import org.example.procedure.procedureRequested.domains.ProcedureRequested;
import org.example.subscription.services.FacilityBrandingService;
import org.example.treatment.domains.TreatmentRequested;
import org.example.treatment.domains.repositories.TreatmentRequestedRepository;
import org.example.visit.domains.PatientVisit;
import org.example.visit.services.paloads.requests.VisitParametersRequest;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class CompassionGroupInvoiceDocxService {

    private static final String TEMPLATE_RESOURCE = "compassion-invoice/compassion-group-invoice-template.docx";
    private static final Pattern DIGIT_SEQUENCE = Pattern.compile("\\d+");
    private static final DateTimeFormatter EXPORT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Inject
    TreatmentRequestedRepository treatmentRequestedRepository;

    @Inject
    PatientGroupRepository patientGroupRepository;

    @Inject
    FacilityBrandingService facilityBrandingService;

    public Response generateDocx(VisitParametersRequest request, List<PatientVisit> visits) {
        if (request == null || request.visitGroup == null || request.visitGroup.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Patient group is required for compassion invoice export.")
                    .build();
        }
        if (request.datefrom == null || request.dateto == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Both datefrom and dateto are required for compassion invoice export.")
                    .build();
        }
        if (visits == null || visits.isEmpty()) {
            String debtHint = request.hasDebt != null
                    ? (request.hasDebt
                        ? " No visits with outstanding debt matched the selected filters."
                        : " No fully paid visits matched the selected filters.")
                    : "";
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No visits found for the selected filters." + debtHint)
                    .build();
        }

        try {
            JsonObject payload = buildPayload(request, visits);
            byte[] docx = runPythonGenerator(payload);
            String filename = "medical-invoice.docx";
            return Response.ok(docx)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to generate compassion invoice: " + e.getMessage())
                    .build();
        }
    }

    private JsonObject buildPayload(VisitParametersRequest request, List<PatientVisit> visits) {
        String facilityName = facilityBrandingService.resolveDefaultBranding().facilityName;
        if (facilityName == null || facilityName.isBlank()) {
            facilityName = "VENERANDA MEDICAL";
        }

        PatientGroup group = patientGroupRepository.findByNormalizedShortForm(request.visitGroup.trim());
        String groupName = group != null && group.groupName != null && !group.groupName.isBlank()
                ? group.groupName
                : request.visitGroup.trim();

        String periodLabel = request.datefrom.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
                .toUpperCase(Locale.ENGLISH);

        List<PatientVisit> sorted = new ArrayList<>(visits);
        sorted.sort(Comparator
                .comparing((PatientVisit v) -> v.visitDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(v -> v.id, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed());

        JsonArrayBuilder visitsJson = Json.createArrayBuilder();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (PatientVisit visit : sorted) {
            JsonObject visitJson = buildVisitJson(visit);
            visitsJson.add(visitJson);
            grandTotal = grandTotal.add(readBigDecimal(visitJson.getJsonNumber("visitTotal")));
        }

        return Json.createObjectBuilder()
                .add("facilityName", facilityName)
                .add("groupName", groupName)
                .add("periodLabel", periodLabel)
                .add("exportDate", LocalDate.now().format(EXPORT_DATE))
                .add("grandTotal", grandTotal)
                .add("visits", visitsJson)
                .build();
    }

    private JsonObject buildVisitJson(PatientVisit visit) {
        Patient patient = visit.patient;
        String diagnosis = resolveDiagnosis(visit);
        BigDecimal discount = resolveDiscount(visit);
        JsonArrayBuilder lines = Json.createArrayBuilder();
        BigDecimal linesTotal = BigDecimal.ZERO;

        if (visit.getProceduresRequested() != null) {
            for (ProcedureRequested procedure : visit.getProceduresRequested()) {
                BigDecimal amount = nz(procedure.totalAmount);
                linesTotal = linesTotal.add(amount);
                lines.add(Json.createObjectBuilder()
                        .add("label", safeText(procedure.procedureRequestedName, "Procedure"))
                        .add("amount", amount)
                        .build());
            }
        }

        List<TreatmentRequested> treatments = treatmentRequestedRepository.list("visit.id", visit.id);
        for (TreatmentRequested treatment : treatments) {
            BigDecimal amount = nz(treatment.totalAmount);
            linesTotal = linesTotal.add(amount);
            lines.add(Json.createObjectBuilder()
                    .add("label", buildTreatmentLabel(treatment))
                    .add("amount", amount)
                    .build());
        }

        BigDecimal visitTotal = nz(visit.totalAmount);
        if (visitTotal.compareTo(BigDecimal.ZERO) <= 0) {
            visitTotal = linesTotal;
        }

        return Json.createObjectBuilder()
                .add("visitDate", visit.visitDate != null ? visit.visitDate.toString() : "")
                .add("patientId", resolvePatientInvoiceId(patient, visit))
                .add("patientName", safeText(visit.patientName, ""))
                .add("age", formatAge(visit.patientAge != null ? visit.patientAge : (patient != null ? patient.patientAge : null)))
                .add("sex", patient != null ? safeText(patient.patientGender, "").toLowerCase(Locale.ENGLISH) : "")
                .add("diagnosis", diagnosis)
                .add("discount", discount)
                .add("visitTotal", visitTotal)
                .add("lines", lines)
                .build();
    }

    private String resolvePatientInvoiceId(Patient patient, PatientVisit visit) {
        if (patient != null) {
            String fromSecondName = extractLastNumber(patient.patientSecondName);
            if (!fromSecondName.isEmpty()) {
                return fromSecondName;
            }
            if (patient.patientFileNo != null && !patient.patientFileNo.isBlank()) {
                return patient.patientFileNo.trim();
            }
            if (patient.id != null) {
                return String.valueOf(patient.id);
            }
        }

        String visitName = visit != null ? safeText(visit.patientName, "") : "";
        if (!visitName.isEmpty()) {
            String[] parts = visitName.trim().split("\\s+");
            if (parts.length > 1) {
                String fromVisitSecondName = extractLastNumber(parts[parts.length - 1]);
                if (!fromVisitSecondName.isEmpty()) {
                    return fromVisitSecondName;
                }
            }
        }
        return "";
    }

    private String extractLastNumber(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        Matcher matcher = DIGIT_SEQUENCE.matcher(value.trim());
        String lastNumber = "";
        while (matcher.find()) {
            lastNumber = matcher.group();
        }
        return lastNumber;
    }

    private String buildTreatmentLabel(TreatmentRequested treatment) {
        StringBuilder label = new StringBuilder(safeText(treatment.itemName, "Treatment"));
        appendPart(label, treatment.amountPerFrequencyValue);
        appendPart(label, treatment.amountPerFrequencyUnit);
        if (treatment.frequencyValue != null) {
            appendPart(label, treatment.frequencyValue.stripTrailingZeros().toPlainString() + " X");
        }
        appendPart(label, treatment.frequencyUnit);
        if (treatment.durationValue != null) {
            appendPart(label, "FOR " + treatment.durationValue.stripTrailingZeros().toPlainString());
        }
        appendPart(label, treatment.durationUnit);
        appendPart(label, treatment.instructions);
        appendPart(label, treatment.route);
        return label.toString().trim();
    }

    private void appendPart(StringBuilder sb, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append(' ');
        }
        sb.append(text);
    }

    private String resolveDiagnosis(PatientVisit visit) {
        if (visit.getConsultation() == null || visit.getConsultation().isEmpty()) {
            return "";
        }
        Consultation first = visit.getConsultation().get(0);
        return first != null ? safeText(first.diagnosis, "") : "";
    }

    private BigDecimal resolveDiscount(PatientVisit visit) {
        if (visit.getInvoice() == null || visit.getInvoice().isEmpty()) {
            return BigDecimal.ZERO;
        }
        Invoice invoice = visit.getInvoice().get(0);
        return invoice != null ? nz(invoice.discount) : BigDecimal.ZERO;
    }

    private String formatAge(BigDecimal age) {
        if (age == null) {
            return "";
        }
        return age.stripTrailingZeros().toPlainString() + "Yrs";
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal readBigDecimal(jakarta.json.JsonNumber number) {
        return number != null ? number.bigDecimalValue() : BigDecimal.ZERO;
    }

    private String safeText(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

    private byte[] runPythonGenerator(JsonObject payload) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("compassion-invoice-");
        Path dataPath = tempDir.resolve("payload.json");
        Path outputPath = tempDir.resolve("invoice.docx");
        Path templatePath = tempDir.resolve("template.docx");

        try {
            try (InputStream templateStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(TEMPLATE_RESOURCE)) {
                if (templateStream == null) {
                    throw new IOException("Template not found on classpath: " + TEMPLATE_RESOURCE);
                }
                Files.copy(templateStream, templatePath, StandardCopyOption.REPLACE_EXISTING);
            }

            Files.writeString(dataPath, payload.toString(), StandardCharsets.UTF_8);

            Path scriptPath = resolveScriptPath();
            ProcessBuilder builder = new ProcessBuilder(
                    resolvePythonCommand(),
                    scriptPath.toString(),
                    "--template", templatePath.toString(),
                    "--data", dataPath.toString(),
                    "--output", outputPath.toString()
            );
            builder.redirectErrorStream(true);
            Process process = builder.start();
            String processOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            if (exitCode != 0 || !Files.exists(outputPath)) {
                throw new IOException("Python invoice generator failed (" + exitCode + "): " + processOutput);
            }
            return Files.readAllBytes(outputPath);
        } finally {
            try {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                                // best effort cleanup
                            }
                        });
            } catch (IOException ignored) {
                // best effort cleanup
            }
        }
    }

    private Path resolveScriptPath() throws IOException {
        Path cwdScript = Path.of(System.getProperty("user.dir"), "scripts", "generate_compassion_group_invoice.py");
        if (Files.exists(cwdScript)) {
            return cwdScript.toAbsolutePath();
        }
        Path parentScript = Path.of(System.getProperty("user.dir"), "hospital", "scripts", "generate_compassion_group_invoice.py");
        if (Files.exists(parentScript)) {
            return parentScript.toAbsolutePath();
        }
        throw new IOException("Could not find generate_compassion_group_invoice.py in scripts folder.");
    }

    private String resolvePythonCommand() {
        String[] candidates = {"python", "python3", "py"};
        for (String candidate : candidates) {
            try {
                Process process = new ProcessBuilder(candidate, "--version").start();
                if (process.waitFor() == 0) {
                    return candidate;
                }
            } catch (Exception ignored) {
                // try next
            }
        }
        return "python";
    }
}
