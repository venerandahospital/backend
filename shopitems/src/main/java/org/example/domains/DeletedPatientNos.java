package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class DeletedPatientNos extends PanacheEntity {

    @Column
    public int deletedPatientNo;


}


