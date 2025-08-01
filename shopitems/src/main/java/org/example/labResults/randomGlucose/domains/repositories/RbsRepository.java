package org.example.labResults.randomGlucose.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.labResults.randomGlucose.domains.Rbs;

@ApplicationScoped
public class RbsRepository implements PanacheRepository<Rbs> {
}
