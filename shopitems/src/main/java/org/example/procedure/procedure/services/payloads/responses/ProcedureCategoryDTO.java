package org.example.procedure.procedure.services.payloads.responses;

import org.example.procedure.procedure.domains.ProcedureCategory;


public class ProcedureCategoryDTO {

    public Long id;
    public String procedureCategory;
    public String categoryDescription;


    public ProcedureCategoryDTO(ProcedureCategory category){
        this.id = category.id;
        this.procedureCategory = category.procedureCategory;
        this.categoryDescription = category.categoryDescription;


    }
}
