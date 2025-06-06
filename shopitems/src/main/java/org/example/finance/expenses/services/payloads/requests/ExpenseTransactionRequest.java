package org.example.finance.expenses.services.payloads.requests;

import org.eclipse.microprofile.openapi.annotations.media.Schema;


import java.math.BigDecimal;


public class ExpenseTransactionRequest {

    @Schema(example = "1")
    public Long userId;

    @Schema(example = "1")
    public Long expenseAccountId;

    @Schema(example = "10000")
    public BigDecimal amountTransacted;

    @Schema(example = "Ronald")
    public String receiver;

    @Schema(example = "Transporting drugs and patients from the facility")
    public String description;
}
