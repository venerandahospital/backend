package org.example.visit;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PatientVisitRepository implements PanacheRepository<PatientVisit> {
}
