package org.example.services.payloads.requests;

import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.example.domains.PatientVisit;

import java.time.LocalDate;

public class RecommendationRequest {

    @Schema(example = "1")
    public Long visitId;  // Link to the specific visit this recommendation is related to

    @Schema(example = "Sleep under mosquito net")
    public String prevention;

    @Schema(example = "review")
    public String recommendationType;

    @Schema(example = "Sleep under mosquito net")
    public String homeAdvice;
}
