package org.example.lab.singleStatementReport.malaria.services.Payloads.responses;


import org.example.lab.singleStatementReport.malaria.domains.Malaria;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MalariaDTO {

    public Long id;
    public Long visitId;
    public Long procedureRequestedId;
    public String test;
    public String bs;
    public String mrdt;
    public String notes;
    public String patientName;
    public BigDecimal patientAge;
    public String gender;
    public String doneBy;
    public String labReportTitle;
    public String recommendation;

    public LocalDateTime sampleCollectionDateAndTime;
    public LocalDateTime procedureDoneDateAndTime;
    public LocalDateTime reportUpDatedDateAndTime;
    public LocalDateTime reportCreationDateAndTime;
    public LocalDate labRequestDate;

    // Constructor to map from Store entity
    public MalariaDTO(Malaria mrdt) {
        this.id = mrdt.id;
        this.visitId = mrdt.visit!= null ? mrdt.visit.id : null;
        this.procedureRequestedId = mrdt.procedureRequested!= null ? mrdt.procedureRequested.id : null;
        this.test = mrdt.test;
        this.notes = mrdt.notes;
        this.gender = mrdt.gender;
        this.patientAge = mrdt.patientAge;
        this.patientName = mrdt.patientName;
        this.doneBy = mrdt.doneBy;
        this.labReportTitle = mrdt.labReportTitle;
        this.bs = mrdt.bs;
        this.mrdt = mrdt.mrdt;
        this.recommendation = mrdt.recommendation;
        this.sampleCollectionDateAndTime = mrdt.sampleCollectionDateAndTime;
        this.procedureDoneDateAndTime = mrdt.procedureDoneDateAndTime;
        this.reportUpDatedDateAndTime = mrdt.reportUpDatedDateAndTime;
        this.reportCreationDateAndTime = mrdt.reportCreationDateAndTime;
        this.labRequestDate = mrdt.labRequestDate;


    }
}
