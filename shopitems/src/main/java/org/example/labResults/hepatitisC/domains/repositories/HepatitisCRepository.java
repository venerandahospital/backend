package org.example.labResults.hepatitisC.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.labResults.hepatitisC.domains.HepatitisC;

@ApplicationScoped
public class HepatitisCRepository implements PanacheRepository<HepatitisC> {
}
