package org.example.procedure.procedure.services.payloads.responses;

import org.example.procedure.procedure.domains.ProcedureType;

public class ProcedureTypeDTO {
    public Long id;
    public String procedureType;
    public String typeDescription;

    public ProcedureTypeDTO(ProcedureType type) {
        if (type != null) {
            this.id = type.id;
            this.procedureType = type.procedureType;
            this.typeDescription = type.typeDescription;
        }
    }
}




