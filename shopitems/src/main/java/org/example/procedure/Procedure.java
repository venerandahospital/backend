package org.example.procedure;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ProcedureTable")
public class Procedure extends PanacheEntity {


    @Column(nullable = false)
    public String procedureType;

    // Category of the lab test (e.g., "Hematology", "Biochemistry")
    @Column
    public String category;

    // Description of the lab test, providing additional information on how its done
    @Column(columnDefinition = "TEXT") // Assuming this may be a longer text
    public String description;

    // Cost price for performing the test (amount in chard by the facility or hospital to perform the test)
    @Column
    public BigDecimal unitCostPrice;

    // Selling price for the test (the amount charged to the patient)
    @Column
    public BigDecimal unitSellingPrice;


    // Name of the lab test (e.g., "Complete Blood Count")
    @Column
    public String procedureName;


}
