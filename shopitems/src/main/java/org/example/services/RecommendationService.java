package org.example.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.domains.PatientVisit;
import org.example.domains.Recommendation;
import org.example.domains.repositories.RecommendationRepository;
import org.example.services.payloads.requests.RecommendationRequest;
import org.example.services.payloads.responses.dtos.RecommendationDTO;

import java.time.LocalDate;

@ApplicationScoped
public class RecommendationService {
    @Inject
    RecommendationRepository recommendationRepository;

    public static final String NOT_FOUND = "Not found!";

    @Transactional
    public RecommendationDTO createNewRecommendation(RecommendationRequest request) {

        PatientVisit patientVisit = PatientVisit.findById(request.visitId);

        // Map request to entity
        Recommendation recommendation = new Recommendation();
        recommendation.recommendationType = request.recommendationType;
        recommendation.prevention = request.prevention;
        recommendation.homeAdvice = request.homeAdvice;
        recommendation.reviewReturnDate = LocalDate.now();
        recommendation.visit = patientVisit;


        // Add items to the lab test

        // Persist the lab test
        recommendationRepository.persist(recommendation);

        return new RecommendationDTO(recommendation);
    }

}
