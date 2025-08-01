package org.example.labResults.cbc.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.labResults.cbc.domains.Cbc;

@ApplicationScoped
public class CbcRepository implements PanacheRepository<Cbc> {
}
