package org.example.services.payloads.responses.dtos;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.example.domains.Patient;


public class PatientDTO {
    public Long id;
    public String patientFirstName;
    public String patientSecondName;
    public String patientAddress;
    public String patientContact;
    public String patientGender;
    public BigDecimal patientAge;
    public LocalDate patientDateOfBirth;
    public String patientProfilePic;
    public LocalDate creationDate;
    public LocalDate patientLastUpdatedDate;
    public String patientFileNo;
    public Integer patientNo;
    public String nextOfKinName;
    public String nextOfKinContact;
    public String relationship;
    public Long patientGroupId;
    public String nextOfKinAddress;
    public List<PatientVisitDTO> patientVisits;

    public PatientDTO(Patient patient) {
        this.id = patient.id;
        this.patientGroupId = patient.patientGroup!= null ? patient.patientGroup.id : null;
        this.patientFirstName = patient.patientFirstName;
        this.patientSecondName = patient.patientSecondName;
        this.patientAddress = patient.patientAddress;
        this.patientContact = patient.patientContact;
        this.patientGender = patient.patientGender;
        this.patientAge = patient.patientAge;
        this.patientDateOfBirth = patient.patientDateOfBirth;
        this.patientProfilePic = patient.patientProfilePic;
        this.creationDate = patient.creationDate;
        this.patientLastUpdatedDate = patient.patientLastUpdatedDate;
        this.patientFileNo = patient.patientFileNo;
        this.patientNo = patient.patientNo;
        this.nextOfKinName = patient.nextOfKinName;
        this.nextOfKinContact = patient.nextOfKinContact;
        this.relationship = patient.relationship;
        this.nextOfKinAddress = patient.nextOfKinAddress;
        this.patientVisits = patient.getPatientVisits().stream().map(PatientVisitDTO::new).toList();
    }
}

