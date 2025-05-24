package org.example.hospitalCafeteria.inventory.stock.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.hospitalCafeteria.inventory.stock.domains.CanteenStock;
import org.example.stock.domains.Stock;

@ApplicationScoped
public class CanteenStockRepository implements PanacheRepository<CanteenStock> {

}
