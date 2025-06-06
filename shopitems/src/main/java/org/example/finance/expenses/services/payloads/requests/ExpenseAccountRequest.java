package org.example.finance.expenses.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ExpenseAccountRequest {

    @Schema(example = "1")
    public Long categoryId;

    @Schema(example = "transport")
    public String accountName;

    @Schema(example = "movement of goods and services")
    public String description;
}
