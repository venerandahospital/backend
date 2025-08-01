package org.example.diagnostics.ultrasoundScan.pelvic.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.ultrasoundScan.pelvic.domains.PelvicUs;

@ApplicationScoped
public class PelvicUsRepository implements PanacheRepository<PelvicUs> {
}
