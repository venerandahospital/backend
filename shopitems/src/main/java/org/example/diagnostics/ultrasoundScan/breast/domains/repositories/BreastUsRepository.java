package org.example.diagnostics.ultrasoundScan.breast.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.ultrasoundScan.breast.domains.BreastUs;

@ApplicationScoped
public class BreastUsRepository implements PanacheRepository<BreastUs> {
}
