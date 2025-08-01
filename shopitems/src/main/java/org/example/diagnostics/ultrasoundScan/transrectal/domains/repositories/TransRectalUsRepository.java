package org.example.diagnostics.ultrasoundScan.transrectal.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.ultrasoundScan.transrectal.domains.TransRectalUs;

@ApplicationScoped
public class TransRectalUsRepository implements PanacheRepository<TransRectalUs> {
}
