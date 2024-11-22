package org.example.domains;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ProcedureTable")
public class Procedure extends PanacheEntity {

    // Name of the lab test (e.g., "Complete Blood Count")
    @Column(nullable = false)
    public String procedureName;

    @Column(nullable = false)
    public String procedureType;

    // Category of the lab test (e.g., "Hematology", "Biochemistry")
    @Column
    public String category;

    // Description of the lab test, providing additional information on how its done
    @Column(columnDefinition = "TEXT") // Assuming this may be a longer text
    public String description;

    // Cost price for performing the test (amount in chard by the facility or hospital to perform the test)
    @Column(nullable = false)
    public BigDecimal unitCostPrice;

    // Selling price for the test (the amount charged to the patient)
    @Column(nullable = false)
    public BigDecimal unitSellingPrice;

    // List of items used for performing this lab test
   /* @OneToMany(mappedBy = "labTest", fetch = FetchType.EAGER)
    private List<ItemUsed> itemsUsed = new ArrayList<>();

    public List<ItemUsed> getItemsUsed() {
        return itemsUsed;
    }

    // Setter for itemsUsed
    public void setItemsUsed(List<ItemUsed> itemsUsed) {
        this.itemsUsed = itemsUsed;
    }*/
}
