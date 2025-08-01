package org.example.labResults.mrdt.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.labResults.mrdt.domains.Mrdt;

@ApplicationScoped
public class MrdtRepository implements PanacheRepository<Mrdt> {
}
