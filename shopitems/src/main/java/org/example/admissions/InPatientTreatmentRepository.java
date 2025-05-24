package org.example.admissions;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InPatientTreatmentRepository implements PanacheRepository<InPatientTreatment> {
}
