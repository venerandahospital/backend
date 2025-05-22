package org.example.consultations;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConsultationRepository implements PanacheRepository<Consultation> {
}
