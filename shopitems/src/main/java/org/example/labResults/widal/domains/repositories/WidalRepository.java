package org.example.labResults.widal.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.labResults.widal.domains.Widal;

@ApplicationScoped
public class WidalRepository implements PanacheRepository<Widal> {
}
