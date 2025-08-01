package org.example.diagnostics.ultrasoundScan.transvaginal.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.ultrasoundScan.transvaginal.domains.TransVaginalUs;

@ApplicationScoped
public class TransVaginalUsRepository implements PanacheRepository<TransVaginalUs> {
}
