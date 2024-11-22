package org.example.services.payloads.requests;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PatientUpdateRequest {
    ///////////////////// patient data//////////////////////////

    @Schema(example = "Tumwesigye")
    public String patientFirstName;

    @Schema(example = "Cryton")
    public String patientSecondName;

    @Schema(example = "Bugogo")
    public String patientAddress;

    @Schema(example = "256784411848")
    public String patientContact;

    @Schema(example = "Male")
    public String patientGender;

    @Schema(example = "28")
    public BigDecimal patientAge;

    @Schema(example = "24/02/1994")
    @JsonbDateFormat(value = "dd/MM/yyyy")
    public LocalDate patientDateOfBirth;  // Matches the "dd/MM/yyyy" format in JSON

    @Schema(example = "Cryton")
    public String patientProfilePic;

    ///////////////////// Next of keen data//////////////////////////

    @Schema(example = "Tumwesigye Benjamin")
    public String nextOfKinName;

    @Schema(example = "256702225307")
    public String nextOfKinContact;

    @Schema(example = "Brother")
    public String relationship;

    @Schema(example = "Kampala")
    public String NextOfKinAddress;

}
