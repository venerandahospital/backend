package org.example.consultations.services.payloads.responses;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.example.consultations.domains.DiagnosisType;

import java.time.LocalDate;

public class DiagnosisTypeDTO {
    public Long id;
    public String title;
    public String description;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate updateDate;

    public DiagnosisTypeDTO() {
    }

    public DiagnosisTypeDTO(DiagnosisType entity) {
        this.id = entity.id;
        this.title = entity.title;
        this.description = entity.description;
        this.creationDate = entity.creationDate;
        this.updateDate = entity.updateDate;
    }
}
