package org.example.diagnostics.ultrasoundScan.obstetric.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.ultrasoundScan.obstetric.domains.ObstetricUs;

@ApplicationScoped
public class ObstetricUsRepository implements PanacheRepository<ObstetricUs> {
}
