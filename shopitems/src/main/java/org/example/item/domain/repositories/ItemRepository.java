package org.example.item.domain.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.item.domain.Item;

@ApplicationScoped
public class ItemRepository implements PanacheRepository<Item> {


}
