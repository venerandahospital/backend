package org.example.diagnostics.ultrasoundScan.abdominal.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.ultrasoundScan.abdominal.domains.AbdominalUs;

@ApplicationScoped
public class AbdominalUsRepository implements PanacheRepository<AbdominalUs> {
}
