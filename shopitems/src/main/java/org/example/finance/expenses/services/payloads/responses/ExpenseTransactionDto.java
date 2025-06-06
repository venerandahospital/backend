package org.example.finance.expenses.services.payloads.responses;

import org.example.finance.expenses.domains.ExpenseTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class ExpenseTransactionDto {

    public Long id;
    public Long userId;
    public Long expenseAccountId;
    public LocalDate dateOfExpenseTransaction;
    public LocalDate upDateOfExpenseTransaction;

    public LocalTime timeOfExpenseTransaction;
    public BigDecimal amountTransacted;
    public String receiver;
    public String referenceNumber;
    public String description;
    public String expenseAccountName;


    public String userName;


    public ExpenseTransactionDto(ExpenseTransaction expenseTransaction) {
        this.dateOfExpenseTransaction = expenseTransaction.dateOfExpenseTransaction;
        this.timeOfExpenseTransaction = expenseTransaction.timeOfExpenseTransaction;
        this.userId = expenseTransaction.user != null ? expenseTransaction.user.id : null;
        this.expenseAccountId = expenseTransaction.expenseAccount != null ? expenseTransaction.expenseAccount.id : null;
        this.id = expenseTransaction.id;
        this.amountTransacted = expenseTransaction.amountTransacted;
        this.receiver = expenseTransaction.receiver;
        this.expenseAccountName = expenseTransaction.expenseAccountName;

        this.description = expenseTransaction.description;
        this.userName = expenseTransaction.userName;

        this.referenceNumber = expenseTransaction.referenceNumber;

        this.upDateOfExpenseTransaction = expenseTransaction.upDateOfExpenseTransaction;

    }
}
