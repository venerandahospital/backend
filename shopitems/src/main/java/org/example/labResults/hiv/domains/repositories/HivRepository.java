package org.example.labResults.hiv.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.labResults.hiv.domains.Hiv;

@ApplicationScoped
public class HivRepository implements PanacheRepository<Hiv> {
}
