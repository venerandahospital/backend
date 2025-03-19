package org.example.domains.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.domains.Patient;

@ApplicationScoped
public class PatientRepository implements PanacheRepository<Patient> {

    /**
     * Finds a patient by their first name and second name, regardless of the order.
     *
     * @param firstName  The first name of the patient.
     * @param secondName The second name of the patient.
     * @return The patient entity if found, otherwise null.
     */
    public Patient findByFirstNameAndSecondName(String firstName, String secondName) {
        return find("(patientFirstName = ?1 and patientSecondName = ?2) or (patientFirstName = ?2 and patientSecondName = ?1)",
                firstName, secondName).firstResult();
    }
}