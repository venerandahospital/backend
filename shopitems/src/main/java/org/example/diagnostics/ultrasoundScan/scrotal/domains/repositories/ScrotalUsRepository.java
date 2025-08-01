package org.example.diagnostics.ultrasoundScan.scrotal.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.ultrasoundScan.scrotal.domains.ScrotalUs;

@ApplicationScoped
public class ScrotalUsRepository implements PanacheRepository<ScrotalUs> {
}
