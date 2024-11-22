package org.example.services.payloads.responses.dtos;

import org.example.domains.Recommendation;

import java.time.LocalDate;

public class RecommendationDTO {
    public Long id;  // The unique identifier of the recommendation
    public Long visitId;  // The ID of the associated patient visit
    public LocalDate reviewReturnDate;  // Follow-up date for the next visit
    public String prevention;  // Preventive measures suggested
    public String recommendationType;  // The type of recommendation (e.g., Follow-up, Medication)
    public String homeAdvice;  // Detailed advice given to the patient

    public RecommendationDTO(Recommendation recommendation) {
        this.id = recommendation.id;
        this.visitId = recommendation.visit != null ? recommendation.visit.id : null;
        this.reviewReturnDate = recommendation.reviewReturnDate;
        this.prevention = recommendation.prevention;
        this.recommendationType = recommendation.recommendationType;
        this.homeAdvice = recommendation.homeAdvice;
    }
}
