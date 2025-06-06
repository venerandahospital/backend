package org.example.finance.expenses.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class ExpenseCategoryRequest {

    @Schema(example = "Meals")
    public String categoryName;

    @Schema(example = "movement of goods and services")
    public String description;
}
