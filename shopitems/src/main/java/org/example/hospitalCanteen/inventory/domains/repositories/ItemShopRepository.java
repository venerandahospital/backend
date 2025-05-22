package org.example.hospitalCanteen.inventory.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.item.domain.Item;

@ApplicationScoped
public class ItemShopRepository implements PanacheRepository<Item> {


}
