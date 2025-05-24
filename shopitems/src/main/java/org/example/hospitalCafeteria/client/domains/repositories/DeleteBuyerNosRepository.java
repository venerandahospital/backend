package org.example.hospitalCafeteria.client.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.hospitalCafeteria.client.domains.DeletedBuyerNos;

@ApplicationScoped
public class DeleteBuyerNosRepository implements PanacheRepository<DeletedBuyerNos> {
}
