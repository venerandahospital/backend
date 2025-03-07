package org.example.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.domains.Payments;

@ApplicationScoped
public class PaymentsRepository implements PanacheRepository<Payments> {

}
