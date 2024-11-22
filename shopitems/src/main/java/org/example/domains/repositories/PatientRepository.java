package org.example.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.domains.Patient;
import org.example.domains.ShoppingCart;

@ApplicationScoped
public class PatientRepository implements PanacheRepository<Patient> {
}
