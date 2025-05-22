package org.example.client.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.client.domains.DeletedPatientNos;

@ApplicationScoped
public class DeletePatientNosRepository implements PanacheRepository<DeletedPatientNos> {
}
