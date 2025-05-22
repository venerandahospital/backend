package org.example.treatment.domains;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TreatmentRequestedRepository implements PanacheRepository<TreatmentRequested> {
}
