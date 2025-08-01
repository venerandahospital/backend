package org.example.diagnostics.ultrasoundScan.abdominalPelvic.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.ultrasoundScan.abdominalPelvic.domains.AbdominalPelvicUs;

@ApplicationScoped
public class AbdominalPelvicUsRepository implements PanacheRepository<AbdominalPelvicUs> {
}
