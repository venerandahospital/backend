package org.example.vitals.services.payloads.responses;

import org.example.vitals.domains.InitialTriageVitals;

import java.time.LocalDate;
import java.time.LocalTime;

public class InitialTriageVitalsDTO {
    public Long id;
    public LocalDate dateTaken;
    public LocalTime timeTaken;
    public Double systolic;
    public Double diastolic;
    public Double map;
    public String bloodPressure;
    public String takenBy;
    public Double pulseRate;
    public Double temperature;
    public Double weight;
    public Double height;
    public String station;
    public Double spO2;
    public Integer heartRate;
    public Integer respiratoryRate;
    public Long visitId;

    public InitialTriageVitalsDTO(InitialTriageVitals initialTriageVitals) {
        this.id = initialTriageVitals.id;
        this.dateTaken = initialTriageVitals.dateTaken;
        this.takenBy = initialTriageVitals.takenBy;
        this.pulseRate = initialTriageVitals.pulseRate;
        this.station = initialTriageVitals.station;
        this.timeTaken = initialTriageVitals.timeTaken;
        this.systolic = initialTriageVitals.systolic;
        this.diastolic = initialTriageVitals.diastolic;
        this.bloodPressure = initialTriageVitals.bloodPressure;
        this.map = initialTriageVitals.map;
        this.temperature = initialTriageVitals.temperature;
        this.weight = initialTriageVitals.weight;
        this.height = initialTriageVitals.height;
        this.spO2 = initialTriageVitals.spO2;
        this.heartRate = initialTriageVitals.heartRate;
        this.respiratoryRate = initialTriageVitals.respiratoryRate;
        this.visitId = initialTriageVitals.visit != null ? initialTriageVitals.visit.id : null;
    }
}
