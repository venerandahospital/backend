package org.example.diagnostics.ultrasoundScan.musculoskeletal.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.diagnostics.ultrasoundScan.musculoskeletal.domains.MskUs;

@ApplicationScoped
public class MskUsRepository implements PanacheRepository<MskUs> {
}
