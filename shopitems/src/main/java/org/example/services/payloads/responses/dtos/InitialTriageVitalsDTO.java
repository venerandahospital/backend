package org.example.services.payloads.responses.dtos;

import org.example.domains.InitialTriageVitals;

import java.time.LocalDate;
import java.time.LocalTime;

public class InitialTriageVitalsDTO {
    public Long id;
    public LocalDate dateTaken;
    public LocalTime timeTaken;
    public String bloodPressure;
    public Double temperature;
    public Double weight;
    public Double height;
    public Double spO2;
    public Integer heartRate;
    public Integer respiratoryRate;
    public Long visitId;

    public InitialTriageVitalsDTO(InitialTriageVitals initialTriageVitals) {
        this.id = initialTriageVitals.id;
        this.dateTaken = initialTriageVitals.dateTaken;
        this.timeTaken = initialTriageVitals.timeTaken;
        this.bloodPressure = initialTriageVitals.bloodPressure;
        this.temperature = initialTriageVitals.temperature;
        this.weight = initialTriageVitals.weight;
        this.height = initialTriageVitals.height;
        this.spO2 = initialTriageVitals.spO2;
        this.heartRate = initialTriageVitals.heartRate;
        this.respiratoryRate = initialTriageVitals.respiratoryRate;
        this.visitId = initialTriageVitals.visit != null ? initialTriageVitals.visit.id : null;
    }
}
