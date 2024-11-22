package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "RecommendationTable")
public class Recommendation extends PanacheEntity {

    // One recommendation is associated with one visit
    @OneToOne
    @JoinColumn(nullable = false)  // Foreign key to link to the PatientVisit
    public PatientVisit visit;  // Link to the specific visit this recommendation is related to

    // A date for the patient to return for follow-up or review (e.g., "Return in 2 weeks")
    @Column
    @JsonbDateFormat(value = "dd/MM/yyyy")
    public LocalDate reviewReturnDate;

    // Preventive measures suggested to the patient (e.g., "Avoid exposure to cold", "Get vaccinated")
    @Column(columnDefinition = "TEXT") // Assuming this may be a longer text
    public String prevention;

    // The type of recommendation (e.g., "Follow-up Appointment", "Medication", "Lifestyle Change")
    @Column
    public String recommendationType;

    // Detailed advice given to the patient at home (e.g., "Increase fluid intake", "Rest for 3 days")
    @Column(columnDefinition = "TEXT") // Assuming this may be a longer text
    public String homeAdvice;
}
