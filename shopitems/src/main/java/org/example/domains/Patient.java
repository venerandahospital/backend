package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Patient extends PanacheEntity {

    ///////////////////// patient data//////////////////////////
    @Column(unique = true,nullable = false)
    public String patientFirstName;

    @Column(unique = true,nullable = false)
    public String patientSecondName;

    @Column
    public String patientAddress;

    @Column
    public String patientContact;

    @Column
    public String patientGender;

    @Column
    public BigDecimal patientAge;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate patientDateOfBirth;

    @Column
    public String patientProfilePic;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate creationDate;

    @Column
    @JsonbDateFormat(value = "yyyy/MM/dd")
    public LocalDate patientLastUpdatedDate;

    @Column(unique = true)
    public String patientFileNo;

    @Column(unique = true)
    public int patientNo;

    ///////////////////// Next of keen data//////////////////////////

    @Column
    public String nextOfKinName;

    @Column
    public String nextOfKinContact;

    @Column
    public String relationship;

    @Column
    public String nextOfKinAddress;

    ///////////////////// Patient Visits //////////////////////////

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    //public List<PatientVisit> patientVisits = new ArrayList<>(); // Initialize to an empty list
    private final List<PatientVisit> patientVisits = new ArrayList<>();

    public List<PatientVisit> getPatientVisits() {
        return patientVisits;
    }








}

