package org.example.services;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.domains.DeletedPatientNos;
import org.example.domains.repositories.DeletePatientNosRepository;

import static io.quarkus.hibernate.orm.panache.PanacheEntityBase.delete;

@ApplicationScoped
public class DeletedPatientNosService {

    @Inject
    DeletePatientNosRepository deletePatientNosRepository;

    @Transactional
    public void saveDeletedPatientNo(int deletedPatientNoFromDeleteMethod){
        DeletedPatientNos dpn = new DeletedPatientNos();
        dpn.deletedPatientNo = deletedPatientNoFromDeleteMethod;

        deletePatientNosRepository.persist(dpn);

    }

    @Transactional
    public int findFirstDeletedPatientNo() {
        return deletePatientNosRepository.listAll(Sort.ascending("deletedPatientNo"))
                .stream()
                .map(DeletedPatientNos -> DeletedPatientNos.deletedPatientNo)
                .findFirst()
                .orElse(0);
    }

    @Transactional
    public void deleteByDeletedPatientNumber(int deletedPatientNumber) {
        deletePatientNosRepository.delete("deletedPatientNo = ?1", deletedPatientNumber);
    }
}
