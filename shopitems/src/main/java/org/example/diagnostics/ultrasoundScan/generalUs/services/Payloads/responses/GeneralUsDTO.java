package org.example.diagnostics.ultrasoundScan.generalUs.services.Payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.diagnostics.ultrasoundScan.generalUs.domains.GeneralUs;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GeneralUsDTO {

    public Long id;
    public Long visitId;
    public Long procedureRequestedId;
    public String indication;
    public String patientName;
    public BigDecimal patientAge;
    public String gender;
    public String doneBy;
    public String exam;
    public String scanReportTitle;
    public String findings;
    public String impression;
    public String recommendation;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate scanPerformingDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate upDatedDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate scanRequestDate;


    // Constructor to map from Store entity
    public GeneralUsDTO(GeneralUs generalUs) {
        this.id = generalUs.id;
        this.visitId = generalUs.visit!= null ? generalUs.visit.id : null;
        this.procedureRequestedId = generalUs.procedureRequested!= null ? generalUs.procedureRequested.id : null;
        this.indication = generalUs.indication;
        this.patientName = generalUs.patientName;
        this.exam = generalUs.exam;
        this.scanReportTitle = generalUs.scanReportTitle;
        this.findings = generalUs.findings;
        this.impression = generalUs.impression;
        this.recommendation = generalUs.recommendation;
        this.scanPerformingDate = generalUs.scanPerformingDate;
        this.upDatedDate = generalUs.upDatedDate;
        this.scanRequestDate = generalUs.scanRequestDate;



    }

}
