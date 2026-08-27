package org.example.pharmacy.otc.services.payloads.responses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.example.treatment.domains.TreatmentRequested;

public class PharmacyDoctorPrescriptionDTO {
    public Long id;
    public Long patientId;
    public Long visitId;
    public Integer visitNumber;
    public LocalDate visitDate;
    public LocalTime visitTime;
    public String patientName;
    public String itemName;
    public Long stockBatchId;
    public Long itemId;
    public String status;
    public Boolean paid;
    public Boolean dispensed;
    public Boolean administered;
    public BigDecimal quantity;
    public BigDecimal unitBuy;
    public BigDecimal unitSellingPrice;
    public String dosage;
    public BigDecimal amountPerFrequencyValue;
    public String amountPerFrequencyUnit;
    public BigDecimal frequencyValue;
    public String frequencyUnit;
    public BigDecimal durationValue;
    public String durationUnit;
    public BigDecimal totalUnits;
    public BigDecimal totalAmount;
    public String instructions;
    public String route;

    public PharmacyDoctorPrescriptionDTO(TreatmentRequested treatment) {
        if (treatment == null || treatment.visit == null) {
            return;
        }
        this.id = treatment.id;
        this.patientId = treatment.visit.patient != null ? treatment.visit.patient.id : null;
        this.visitId = treatment.visit.id;
        this.visitNumber = treatment.visit.visitNumber;
        this.visitDate = treatment.visit.visitDate;
        this.visitTime = treatment.visit.visitTime;
        this.patientName = PharmacyDoctorPrescriptionDTO.resolvePatientName(treatment);
        this.itemName = treatment.itemName;
        if (treatment.stockBatch != null) {
            this.stockBatchId = treatment.stockBatch.id;
        }
        this.itemId = treatment.itemId;
        this.status = treatment.status != null ? treatment.status : "pending";
        this.paid = treatment.paid == null || Boolean.TRUE.equals(treatment.paid);
        this.dispensed = treatment.isDispensedOrGiven();
        this.administered = Boolean.TRUE.equals(treatment.administered);
        this.quantity = treatment.quantity;
        this.unitBuy = treatment.unitBuy;
        this.unitSellingPrice = treatment.unitSellingPrice;
        this.amountPerFrequencyValue = treatment.amountPerFrequencyValue;
        this.amountPerFrequencyUnit = treatment.amountPerFrequencyUnit;
        this.dosage = PharmacyDoctorPrescriptionDTO.formatDosage(treatment.amountPerFrequencyValue, treatment.amountPerFrequencyUnit);
        this.frequencyValue = treatment.frequencyValue;
        this.frequencyUnit = treatment.frequencyUnit;
        this.durationValue = treatment.durationValue;
        this.durationUnit = treatment.durationUnit;
        this.totalUnits = treatment.totalUnits;
        this.totalAmount = treatment.totalAmount;
        this.instructions = treatment.instructions;
        this.route = treatment.route;
    }

    private static String resolvePatientName(TreatmentRequested treatment) {
        if (treatment.visit.patientName != null && !treatment.visit.patientName.isBlank()) {
            return treatment.visit.patientName.trim();
        }
        if (treatment.visit.patient != null) {
            String first = treatment.visit.patient.patientFirstName != null ? treatment.visit.patient.patientFirstName : "";
            String second = treatment.visit.patient.patientSecondName != null ? treatment.visit.patient.patientSecondName : "";
            return (first + " " + second).trim();
        }
        return "\u2014";
    }

    private static String formatDosage(BigDecimal value, String unit) {
        if (value != null && value.compareTo(BigDecimal.ZERO) == 0) {
            value = null;
        }
        if (value == null && (unit == null || unit.isBlank())) {
            return null;
        }
        if (value == null) {
            return unit.trim();
        }
        String amount = value.stripTrailingZeros().toPlainString();
        if (unit == null || unit.isBlank()) {
            return amount;
        }
        return amount + unit.trim();
    }
}
