package org.example.store.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.store.domains.Store;
import org.example.store.domains.repositories.StoreRepository;
import org.example.store.services.payloads.requests.StoreRequest;
import org.example.store.services.payloads.responses.StoreDTO;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class StoreService {

    @Inject
    StoreRepository storeRepository;

    @Transactional
    public StoreDTO createNewStore(StoreRequest request){

        Store store = new Store();
        store.name = request.name;
        store.location = request.location;
        store.description = request.description;
        store.creationDate = LocalDate.now();

        storeRepository.persist(store);

        return new StoreDTO(store);

    }

    @Transactional
    public List<Store> getAllStores() {
        return storeRepository.listAll(Sort.descending("creationDate"));
    }



}
