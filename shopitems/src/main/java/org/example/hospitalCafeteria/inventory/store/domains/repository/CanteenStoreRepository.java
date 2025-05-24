package org.example.hospitalCafeteria.inventory.store.domains.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.hospitalCafeteria.inventory.store.domains.CanteenStore;

@ApplicationScoped
public class CanteenStoreRepository implements PanacheRepository<CanteenStore> {
}
