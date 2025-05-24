package org.example.hospitalCafeteria.inventory.item.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.hospitalCafeteria.inventory.item.domains.CanteenItem;

@ApplicationScoped
public class CanteenItemRepository implements PanacheRepository<CanteenItem> {


}
