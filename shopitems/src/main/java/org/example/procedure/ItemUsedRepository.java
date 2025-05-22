package org.example.procedure;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ItemUsedRepository implements PanacheRepository<ItemUsed> {
}
