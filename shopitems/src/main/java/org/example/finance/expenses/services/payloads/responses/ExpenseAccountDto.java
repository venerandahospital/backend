package org.example.finance.expenses.services.payloads.responses;

import org.example.finance.expenses.domains.ExpenseAccount;

import java.time.LocalDate;
import java.time.LocalTime;

public class ExpenseAccountDto {

    public Long id;
    public String accountName;
    public String description;
    public LocalDate dateOfAccountCreation;
    public LocalDate dateOfAccountUpdate;
    public LocalTime timeOfAccountCreation;


    // Constructor to map LabTestsRequested to LabTestsRequestedDTO
    public ExpenseAccountDto(ExpenseAccount expenseAccount) {
        this.dateOfAccountCreation = expenseAccount.dateOfAccountCreation;
        this.timeOfAccountCreation = expenseAccount.timeOfAccountCreation;

        this.id = expenseAccount.id;
        this.accountName = expenseAccount.accountName;
        this.description = expenseAccount.description;
        this.dateOfAccountUpdate = expenseAccount.dateOfAccountUpdate;

    }
}
