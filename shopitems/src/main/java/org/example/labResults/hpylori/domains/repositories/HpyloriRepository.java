package org.example.labResults.hpylori.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.labResults.hpylori.domains.Hpylori;

@ApplicationScoped
public class HpyloriRepository implements PanacheRepository<Hpylori> {
}
