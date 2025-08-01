package org.example.labResults.urineHcg.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.labResults.urineHcg.domains.UrineHcg;

@ApplicationScoped
public class UrineHcgRepository implements PanacheRepository<UrineHcg> {
}
